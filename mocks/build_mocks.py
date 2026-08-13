#!/usr/bin/env python3
"""Build two unique 135-question CPMAI mock exams from the 270-item bank."""
import json
import math
import random
import re
from pathlib import Path

ROOT = Path("/workspace")
SRC = Path("/home/ubuntu/.cursor/projects/workspace/uploads")
OUT = ROOT / "mocks"
TEMPLATE = (OUT / "exam_template.html").read_text(encoding="utf-8")

QCOUNT = 135
PASS_MARK = math.ceil(QCOUNT * 0.70)


def extract_questions(html: str):
    start = html.find("const QUESTIONS =")
    start = html.find("[", start)
    end = html.find("\n    const DIFFICULTIES", start)
    blob = html[start:end].rstrip()
    if blob.endswith(";"):
        blob = blob[:-1]
    return json.loads(blob.strip(), strict=False)


def load_bank():
    bank = []
    for p in sorted(SRC.glob("*.txt")):
        html = p.read_text(encoding="utf-8")
        for q in extract_questions(html):
            bank.append({
                "question": q["question"],
                "options": q["options"],
                "correct": q["correct"],
                "correctExplanation": q["correctExplanation"],
                "incorrectExplanations": q.get("incorrectExplanations") or [],
                "source": p.name,
            })
    return bank


def slim(q, exam_id):
    return {
        "id": exam_id,
        "question": q["question"],
        "options": q["options"],
        "correct": q["correct"],
        "correctExplanation": q["correctExplanation"],
        "incorrectExplanations": q["incorrectExplanations"],
    }


def split_unique(bank):
    rng = random.Random(20260813)
    groups = {}
    for q in bank:
        groups.setdefault(q["source"], []).append(q)
    for items in groups.values():
        rng.shuffle(items)

    buckets = [[], []]
    sources = list(groups.keys())
    idx = 0
    remaining = True
    while remaining:
        remaining = False
        for src in sources:
            if groups[src]:
                remaining = True
                buckets[idx % 2].append(groups[src].pop())
                idx += 1

    for i, b in enumerate(buckets, 1):
        rng.shuffle(b)
        if len(b) != QCOUNT:
            raise SystemExit(f"Mock {i} has {len(b)} questions, expected {QCOUNT}")
    return buckets


def write_exam(n, questions):
    payload = [slim(q, i + 1) for i, q in enumerate(questions)]
    html = (
        TEMPLATE.replace("{{TITLE}}", f"PMI-CPMAI Mock Exam {n}")
        .replace("{{QUESTIONS}}", json.dumps(payload, ensure_ascii=False))
        .replace("{{QCOUNT}}", str(QCOUNT))
        .replace("{{PASS_MARK}}", str(PASS_MARK))
    )
    path = OUT / f"mock-exam-{n}.html"
    path.write_text(html, encoding="utf-8")
    return path


INDEX = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>PMI-CPMAI Mock Exams</title>
  <style>
    :root {{ --bg:#0b1419; --text:#e8f1f5; --muted:#8aa3b3; --accent:#2aa889; --border:#2c4a5a; --card:#1a2d38; }}
    body {{ margin:0; font-family:"Segoe UI","Helvetica Neue",Arial,sans-serif; background:#0b1419; color:var(--text); }}
    .wrap {{ width:min(880px, calc(100% - 2rem)); margin:0 auto; padding:2rem 0 3rem; }}
    h1 {{ margin:0 0 .4rem; }}
    p {{ color:var(--muted); line-height:1.55; }}
    .grid {{ display:grid; gap:1rem; margin-top:1.25rem; }}
    a.card {{
      display:block; text-decoration:none; color:inherit; background:var(--card);
      border:1px solid var(--border); border-radius:14px; padding:1.2rem 1.3rem;
    }}
    a.card:hover {{ border-color:var(--accent); }}
    .kicker {{ color:var(--accent); font-size:.78rem; font-weight:700; letter-spacing:.08em; text-transform:uppercase; }}
    h2 {{ margin:.35rem 0 .4rem; font-size:1.25rem; }}
  </style>
</head>
<body>
  <div class="wrap">
    <div class="kicker">CPMAI Study Hall</div>
    <h1>Two unique 135-question mock exams</h1>
    <p>
      Both mocks use only your 270 practice-bank questions. Each exam is 135 items,
      160 minutes, no difficulty labels, and no answers until you submit.
      The two papers do not overlap, so together they cover all 270 questions.
    </p>
    <div class="grid">
      <a class="card" href="mock-exam-1.html">
        <div class="kicker">Form A</div>
        <h2>Mock Exam 1</h2>
        <p>{QCOUNT} items · 160:00 countdown · navigator, flagging, end-of-exam answer key</p>
      </a>
      <a class="card" href="mock-exam-2.html">
        <div class="kicker">Form B</div>
        <h2>Mock Exam 2</h2>
        <p>{QCOUNT} items · 160:00 countdown · navigator, flagging, end-of-exam answer key</p>
      </a>
    </div>
  </div>
</body>
</html>
"""


def coverage_report(bank, buckets):
    bank_stems = {re.sub(r"\s+", " ", q["question"].strip().lower()) for q in bank}
    all_stems = []
    for i, b in enumerate(buckets, 1):
        stems = [re.sub(r"\s+", " ", q["question"].strip().lower()) for q in b]
        if len(stems) != len(set(stems)):
            raise SystemExit(f"Duplicate inside mock {i}")
        all_stems.append(set(stems))
    if all_stems[0] & all_stems[1]:
        raise SystemExit("Mocks overlap")
    union = all_stems[0] | all_stems[1]
    missing = bank_stems - union
    extra = union - bank_stems
    if missing or extra:
        raise SystemExit(f"Coverage error missing={len(missing)} extra={len(extra)}")
    print(f"OK: 2 unique mocks, {QCOUNT} each, all 270 bank questions covered, no added items")


def main():
    bank = load_bank()
    if len(bank) != 270:
        raise SystemExit(f"Expected 270 bank questions, got {len(bank)}")
    buckets = split_unique(bank)
    coverage_report(bank, buckets)
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "index.html").write_text(INDEX, encoding="utf-8")
    for i, bucket in enumerate(buckets, 1):
        write_exam(i, bucket)
        print(f"  Mock {i}: {len(bucket)} bank questions")
    old = OUT / "mock-exam-3.html"
    if old.exists():
        old.unlink()
        print("  removed mock-exam-3.html")


if __name__ == "__main__":
    main()
