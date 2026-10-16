#!/usr/bin/env python3
from pathlib import Path
import re
import sys
ROOT = Path(__file__).resolve().parents[1]


def require(condition: bool, message: str) -> None:
    if not condition:
        print(f"ERROR: {message}", file=sys.stderr)
        raise SystemExit(1)


required_files = [
    "compose.yaml",
]
for relative in required_files:
    require((ROOT / relative).is_file(), f"missing {relative}")

compose = (ROOT / "compose.yaml").read_text(encoding="utf-8")
require("build:" not in compose, "student Compose must consume the published image")
require("ghcr.io/" in compose, "Compose must default to a GHCR image")
require("./datasets:/home/public:ro" in compose, "datasets mount must be read-only")
require("./workspace:/home/student/Project5:rw" in compose, "workspace mount must be read-write")
for binding in ("127.0.0.1:9870:9870", "127.0.0.1:8088:8088", "127.0.0.1:19888:19888"):
    require(binding in compose, f"missing loopback-only port binding {binding}")

scaffolds = {
    1: "LetterCounter",
    2: "FindPattern",
    4: "MinTemperature",
    5: "AggravatedAssaultsPlusRobberies",
    6: "OaklandCrimeStats",
    7: "OaklandCrimeStatsKML",
}
for task, class_name in scaffolds.items():
    source = ROOT / f"workspace/Part_1/Task{task}/{class_name}.java"
    require(source.is_file(), f"missing Task {task} scaffold")
    text = source.read_text(encoding="utf-8")
    require(re.search(rf"public class {class_name}\s*\{{", text) is not None, f"wrong Task {task} class")
    require("public static void main(String[] args) throws Exception" in text, f"wrong Task {task} main signature")
    for forbidden in ("Mapper", "Reducer", "Job.getInstance", "ToolRunner"):
        require(forbidden not in text, f"Task {task} scaffold exposes {forbidden}")

active_docs = (ROOT / "README.md").read_text(encoding="utf-8")

for stale_reference in ("jumbo2.heinz.cmu.local", "Cluster Password Quiz"):
    require(stale_reference not in active_docs, f"active documentation contains {stale_reference}")

print("Repository contracts are valid.")
