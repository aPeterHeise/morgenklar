#!/usr/bin/env python3
"""Use the signed GitHub release APK unchanged for Firebase distribution."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import time


def release_filenames(tag):
    if not re.fullmatch(r"v\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?", tag):
        raise ValueError("Expected a version tag such as v0.1.1")
    return f"morgenklar-{tag[1:]}.apk", f"SHA256-{tag[1:]}.txt"


def validate_release(release, tag):
    if release.get("isDraft") or release.get("tagName") != tag:
        raise ValueError("Only the requested, published release can be distributed")
    filenames = release_filenames(tag)
    assets = release.get("assets", [])
    for name in filenames:
        matches = [asset for asset in assets if asset["name"] == name]
        if len(matches) != 1 or matches[0].get("state") != "uploaded":
            return False  # Release publication can arrive before all assets.
    return True


def verify_apk(apk, checksum, asset):
    expected = checksum.read_text().strip().split()
    if (len(expected) != 2 or expected[1].lstrip("*") != apk.name
            or not re.fullmatch(r"[0-9a-fA-F]{64}", expected[0])):
        raise ValueError("Checksum file must name exactly the release APK")
    actual = hashlib.sha256(apk.read_bytes()).hexdigest()
    if actual != expected[0].lower():
        raise ValueError("APK SHA-256 does not match the published checksum")
    if apk.stat().st_size != asset["size"]:
        raise ValueError("APK size does not match the GitHub release asset")
    if asset.get("digest") and asset["digest"] != f"sha256:{actual}":
        raise ValueError("APK SHA-256 does not match GitHub's asset digest")
    return actual


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", required=True)
    parser.add_argument("--tag", required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    apk_name, checksum_name = release_filenames(args.tag)
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    for attempt in range(7):
        result = subprocess.run([
            "gh", "release", "view", args.tag, "--repo", args.repo,
            "--json", "tagName,isDraft,assets,body,url",
        ], check=True, capture_output=True, text=True)
        release = json.loads(result.stdout)
        if validate_release(release, args.tag):
            break
        if attempt == 6:
            raise ValueError(f"Release needs uploaded assets {apk_name} and {checksum_name}")
        time.sleep(10)
    subprocess.run([
        "gh", "release", "download", args.tag, "--repo", args.repo,
        "--pattern", apk_name, "--pattern", checksum_name,
        "--dir", str(output), "--clobber",
    ], check=True)
    apk = output / apk_name
    asset = next(a for a in release["assets"] if a["name"] == apk_name)
    digest = verify_apk(apk, output / checksum_name, asset)
    notes = output / "release-notes.txt"
    notes.write_text(f"Morgenklar {args.tag}\n{release['url']}\n\n{release.get('body', '')}\n")
    if os.environ.get("GITHUB_OUTPUT"):
        with open(os.environ["GITHUB_OUTPUT"], "a") as handle:
            handle.write(f"apk={apk}\nnotes={notes}\n")
    print(f"Verified {apk.name}: SHA-256 {digest}")


if __name__ == "__main__":
    main()
