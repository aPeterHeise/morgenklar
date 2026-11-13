import hashlib
from pathlib import Path
import tempfile
import unittest

from prepare_firebase_release import release_filenames, validate_release, verify_apk


class ReleaseVerificationTest(unittest.TestCase):
    def test_only_version_tags_are_accepted(self):
        self.assertEqual(release_filenames("v0.1.1"), ("morgenklar-0.1.1.apk", "SHA256-0.1.1.txt"))
        for tag in ["main", "../../v0.1.1", "v0.1.1\nother", "--help", "v$(whoami)"]:
            with self.assertRaises(ValueError):
                release_filenames(tag)

    def test_drafts_and_wrong_releases_are_rejected(self):
        for release in [{"isDraft": True, "tagName": "v0.1.1"}, {"tagName": "v0.1.0"}]:
            with self.assertRaises(ValueError):
                validate_release(release, "v0.1.1")

    def test_waits_for_both_completed_assets(self):
        release = {"tagName": "v0.1.1", "isDraft": False, "assets": []}
        self.assertFalse(validate_release(release, "v0.1.1"))
        release["assets"] = [{"name": n, "state": "uploaded"} for n in release_filenames("v0.1.1")]
        self.assertTrue(validate_release(release, "v0.1.1"))
        release["assets"][0]["state"] = "starter"
        self.assertFalse(validate_release(release, "v0.1.1"))

    def test_detects_tampering_and_wrong_checksum_filename(self):
        with tempfile.TemporaryDirectory() as directory:
            apk = Path(directory) / "morgenklar-0.1.1.apk"
            checksum = Path(directory) / "SHA256-0.1.1.txt"
            apk.write_bytes(b"signed release fixture")
            digest = hashlib.sha256(apk.read_bytes()).hexdigest()
            asset = {"size": apk.stat().st_size, "digest": "sha256:" + digest}
            checksum.write_text(f"{digest}  {apk.name}\n")
            self.assertEqual(verify_apk(apk, checksum, asset), digest)
            checksum.write_text(f"{digest}  other.apk\n")
            with self.assertRaises(ValueError):
                verify_apk(apk, checksum, asset)
            checksum.write_text(f"{digest}  {apk.name}\n")
            apk.write_bytes(b"modified release fixture")
            with self.assertRaises(ValueError):
                verify_apk(apk, checksum, asset)


if __name__ == "__main__":
    unittest.main()
