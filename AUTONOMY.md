# Autonomous development policy

## Mission
Maintain and improve this project toward the objectives in
.autonomy/vision.md, within its constraints and goals.

## Each development cycle
1. Inspect Git status, the current task, backlog, and recent progress.
2. Recover or document unfinished work before starting unrelated work.
3. Check the project baseline using the approved validation commands.
4. If no tasks or code exist, write tasks and code. If they exist, select one task.
5. Implement the task.
6. Run relevant tests and the required project checks.
7. Inspect the diff for regressions, scope creep, and accidental changes.
8. Record the actual outcome, evidence, and next action in .autonomy/.
9. Finish this cycle. The scheduler will start the next one.

## When the backlog is empty
Inspect the project for evidence-backed improvements:
- reproducible defects;
- missing tests around important behavior;
- measurable performance problems;
- documentation inconsistent with actual behavior;
- roadmap gaps relevant to the mission.

Create a small number of ranked tasks with acceptance criteria.
Do not invent features merely to remain busy.
If no worthwhile task exists, record IDLE and explain why.

## Failure policy
Do not repeat the same unsuccessful approach more than twice.
Record failed approaches and the evidence that they failed.
Mark tasks BLOCKED when credentials, product decisions, or external
dependencies prevent progress. Continue with independent work.

## Boundaries
Work only in the designated checkout and development environment.
Do not change credentials.
Do not weaken tests, security checks, or acceptance criteria to claim success.
Do not modify this policy or supervisor controls.
Do not discard pre-existing human changes.
