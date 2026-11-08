#!/bin/sh
# 0.75.0: after (re)building forge guns: optics/suppressors + tables, paint layers, accessory anchors
set -e
cd "$(dirname "$0")/.."
python3 tools/gunsmith_072.py > /tmp/gunsmith.out
python3 tools/update_tables.py
python3 tools/make_0700_paint.py | tail -1
python3 tools/acc_anchors.py > /dev/null
python3 tools/gunforge.py
echo forge ok
