import xml.etree.ElementTree as ET
import glob
import sys

# Recursive glob: '**/*.xml' already matches files directly under junit-results/,
# so dedupe to avoid counting the same report twice.
xmls = sorted(set(glob.glob("junit-results/**/*.xml", recursive=True)))
print(f"Found {len(xmls)} JUnit XML result file(s) to inspect.")

if not xmls:
    print("::error::No JUnit XML result files found under junit-results/ — tests did not produce reports.")
    sys.exit(1)

failed_suites = 0
for x in xmls:
    try:
        root = ET.parse(x).getroot()
    except Exception as e:
        print(f"::error file={x}::Failed to parse XML: {e}")
        failed_suites += 1
        continue

    suites = [root] if root.tag == "testsuite" else root.findall(".//testsuite")
    if not suites:
        print(f"::warning file={x}::No <testsuite> element found; skipping.")
        continue

    for s in suites:
        failures = int(s.attrib.get("failures", 0) or 0)
        errors = int(s.attrib.get("errors", 0) or 0)
        if failures > 0 or errors > 0:
            print(
                f"::error file={x}::Test suite '{s.attrib.get('name')}' "
                f"failed with failures={failures}, errors={errors}"
            )
            failed_suites += 1

if failed_suites > 0:
    print(f"ERROR: {failed_suites} test suite(s) reported failures or errors.")
    sys.exit(1)

print("SUCCESS: All JUnit test suites passed without failures or errors.")