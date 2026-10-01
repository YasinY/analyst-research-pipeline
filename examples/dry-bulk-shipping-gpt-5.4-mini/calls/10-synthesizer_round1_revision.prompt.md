# synthesizer/round1/revision

## System prompt

You are the synthesis agent of a research pipeline used by financial analysts.

Your only job is to write a short briefing for a non-technical analyst from the evidence you are given. The evidence has already been collected, compared, and scored by other parts of the pipeline. You do not add knowledge of your own, you do not change the confidence scores, and you do not resolve conflicts that the evidence leaves open.

Rules:
- Every statement in summary, keyFacts, and uncertainties must cite the ids of the evidence groups it is based on, in groupIds. A statement without a groupId will be removed before the briefing is shown. Put the ids only in groupIds, never inside the text itself.
- The summary answers the analyst query directly in 3 to 5 sentences and touches every sub-question that has evidence, including the risk or outlook dimension if the query asks for it.
- Write 5 to 10 key facts that together cover every sub-question with eligible evidence. Qualitative findings from eligible groups, such as a named risk driver, are key facts too, not uncertainties.
- Uncertainties are reserved for three things: open conflicts between groups, weak evidence, and gaps. Do not describe two compatible facts as if they disagreed.
- Key facts may only be built from evidence groups listed under "Evidence eligible for key facts". Each key fact should rest on one group, or on several groups that agree.
- Groups listed under "Weak evidence" may only appear in uncertainties, phrased as unconfirmed or weakly supported, never as fact.
- Where two groups conflict, say so explicitly, give both values, and name both groups. Never average, pick a side, or smooth the conflict away.
- Evidence groups are formed per sub-question, so the same quantity can appear in one group without a conflict and in another group with an open conflict. Treat such a quantity as conflicted everywhere: do not state it as a key fact from the unconflicted group.
- Every sub-question listed under "Gaps" must appear in uncertainties with a plain statement that no adequate evidence was found for it.
- Use only numbers, dates, and names that appear in the evidence. Do not round, extrapolate, or add context from memory.
- Never write about the briefing itself, its sections, its evidence groups or what is "shown above"; write only about the subject matter.
- Write in plain English for a reader without technical or domain background. Short sentences. No jargon without a short explanation.
- Suggest 3 to 5 follow-up questions an analyst could pursue next, focused on the gaps and conflicts.
- If you are given a previous draft and review findings, produce a corrected draft that resolves every finding. Keep what was not criticised.

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "summary": [
    { "text": "one sentence", "groupIds": ["q1-g1"] }
  ],
  "keyFacts": [
    { "text": "one factual sentence", "groupIds": ["q1-g1"] }
  ],
  "uncertainties": [
    { "text": "one sentence naming a conflict, a weak finding, or a gap", "groupIds": ["q2-g1", "q2-g2"] }
  ],
  "followUpQuestions": [
    "a question an analyst could pursue next"
  ]
}

For a gap with no evidence group, use an empty groupIds list.


## User prompt

Analyst query:

Give me an overview of the dry bulk shipping market and its main risk drivers.

How the planner understood the query:

The analyst is asking for a factual overview of the dry bulk shipping market and the main identifiable drivers of risk affecting it.

Evidence eligible for key facts:

[g-q1-c1] confidence MEDIUM (0.60): Total seaborne dry bulk trade in 2025 was 5.5 billion tonnes and grew by 1.1 percent year on year. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c2] confidence MEDIUM (0.60): Iron ore and coal together accounted for 55 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c3] confidence MEDIUM (0.60): Grain and soybeans accounted for 10 percent of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c4] confidence MEDIUM (0.60): Minor bulks made up the remaining share of seaborne dry bulk volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c11] confidence HIGH (0.90): China accounted for about 71 percent of seaborne iron ore imports in 2025, importing 1.15 billion tonnes. | 3 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g-q1-c6] confidence MEDIUM (0.60): Grain trade shifted toward South American exporters after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q1-c7] confidence HIGH (0.75): Tonne-mile demand grew faster than tonnes in 2025 because of longer Atlantic-to-Pacific routes, with tonne-miles up 2.3 percent. | 2 independent source(s), best tier A, newest 2026-05-20, conflict NONE
[g-q1-c10] confidence MEDIUM (0.60): Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent year on year. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c12] confidence MEDIUM (0.60): Australia supplied 56 percent of seaborne iron ore volumes in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c13] confidence MEDIUM (0.60): Brazil supplied 24 percent of seaborne iron ore volumes in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q1-c14] confidence MEDIUM (0.60): Iron ore was the single largest dry bulk commodity ahead of coal and grain in 2025. | 1 independent source(s), best tier A, newest 2026-01-28, conflict NONE
[g-q2-c8] confidence MEDIUM (0.40): Chinese steel demand and property construction were identified as important 2026 risk drivers for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q2-c9] confidence MEDIUM (0.40): The ramp-up of the Simandou iron ore project in Guinea was identified as an important 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q2-c10] confidence MEDIUM (0.40): Shifts in grain trade flows between the Americas and Asia were identified as an important 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q3-c1] confidence MEDIUM (0.55): The dry bulk fleet grew in 2025 to about 1,020 million dwt, with fleet growth estimated at 2.4 to 3.1 percent. | 2 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c2] confidence MEDIUM (0.55): The dry bulk orderbook was about 9.8 to 10.2 percent of the fleet. | 2 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c3] confidence MEDIUM (0.55): Dry bulk deliveries in 2025 were around 30.1 to 31.6 million dwt, with removals or scrapping reported as very low. | 2 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c8] confidence MEDIUM (0.40): Net fleet growth in 2026 was expected to be about 2.0 to 2.5 percent. | 1 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q3-c10] confidence MEDIUM (0.40): Cape of Good Hope diversions added 1 to 2 percent to dry bulk tonne-mile demand in 2025, and a return to Suez routing would release about 2 percent of fleet capacity. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q3-c11] confidence MEDIUM (0.40): A return to Suez routing was described as a major downside risk to freight rates in 2026. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q3-c12] confidence MEDIUM (0.40): Port congestion was identified as a main 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q4-c1] confidence MEDIUM (0.45): The composite dry bulk freight index averaged about 1,650 points in 2025, roughly 9 percent below 2024. | 2 independent source(s), best tier A, newest 2026-01-22, conflict OPEN | conflicts with g-q4-c4: The composite dry bulk freight index cannot both average about 1,650 points in 2025 and end 2025 at 1,480 points, because those are different values for the same index and period-end versus full-year average.
[g-q4-c2] confidence MEDIUM (0.60): Capesize earnings ranged between 8,000 and 34,000 US dollars per day within 2025. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q4-c3] confidence MEDIUM (0.60): Panamax and Supramax earnings averaged close to 12,000 US dollars per day in 2025. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q5-c5] confidence MEDIUM (0.40): Bunker fuel prices were identified as a main 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c6] confidence MEDIUM (0.40): Geopolitical disruption of routes including Red Sea diversions was identified as a main 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q1-c8] confidence MEDIUM (0.40): Dry bulk earnings in 2026 are affected by Chinese steel demand and property construction, the ramp-up of the Simandou iron ore project in Guinea, shifts in grain trade flows between the Americas and Asia, port congestion, bunker fuel prices, and geopolitical disruption of routes including Red Sea diversions. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q2-c13] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025 as domestic production rose and hydro output recovered. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c14] confidence MEDIUM (0.40): Indian coal imports grew 4 percent and partly offset the Chinese decline. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c15] confidence MEDIUM (0.40): The dry bulk index averaged about 1,650 points in 2025, down roughly 9 percent year on year. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q2-c16] confidence MEDIUM (0.40): Capesize volatility was driven by Brazilian iron ore timing and Chinese restocking. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q2-c17] confidence MEDIUM (0.40): 2026 rates were expected to be supported by low fleet growth but capped by soft Chinese steel demand. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE

Weak evidence (may only appear in uncertainties):

[g-q4-c4] confidence LOW (0.30): The composite dry bulk freight index ended 2025 at 1,480 points. | 1 independent source(s), best tier A, newest 2026-01-15, conflict OPEN | conflicts with g-q4-c1: The composite dry bulk freight index cannot both average about 1,650 points in 2025 and end 2025 at 1,480 points, because those are different values for the same index and period-end versus full-year average.

<!-- cache-boundary -->

Gaps (sub-questions without adequate evidence):

(none)

Revision context:

Previous draft:
Summary:
- The dry bulk market carried 5.5 billion tonnes in 2025, up 1.1 percent year on year, and the main cargoes were iron ore and coal, which together made up 55 percent of volumes. [The market was led by iron ore, with China taking about 71 percent of seaborne iron ore imports, and tonne-mile demand grew faster than tonnes because of longer Atlantic-to-Pacific routes.] (groups: g-q1-c1, g-q1-c2, g-q1-c11, g-q1-c7)
- Freight conditions were softer in 2025, with the composite dry bulk freight index averaging about 1,650 points, roughly 9 percent below 2024, while Capesize earnings ranged between 8,000 and 34,000 US dollars per day and Panamax and Supramax earnings averaged close to 12,000 US dollars per day. (groups: g-q4-c1, g-q2-c15, g-q4-c2, g-q4-c3)
- The main 2026 risk drivers are Chinese steel demand and property construction, the ramp-up of the Simandou iron ore project in Guinea, shifts in grain trade flows between the Americas and Asia, port congestion, bunker fuel prices, geopolitical disruption of routes including Red Sea diversions, and a possible return to Suez routing. (groups: g-q2-c8, g-q2-c9, g-q2-c10, g-q3-c12, g-q5-c5, g-q5-c6, g-q3-c11)

Key facts:
- Total seaborne dry bulk trade in 2025 was 5.5 billion tonnes and grew by 1.1 percent year on year. (groups: g-q1-c1)
- Iron ore and coal together accounted for 55 percent of seaborne dry bulk volumes in 2025. (groups: g-q1-c2)
- Grain and soybeans accounted for 10 percent of seaborne dry bulk volumes in 2025. (groups: g-q1-c3)
- Minor bulks made up the remaining share of seaborne dry bulk volumes in 2025. (groups: g-q1-c4)
- Iron ore was the single largest dry bulk commodity ahead of coal and grain in 2025. (groups: g-q1-c14)
- China accounted for about 71 percent of seaborne iron ore imports in 2025, importing 1.15 billion tonnes. (groups: g-q1-c11)
- Seaborne iron ore trade reached 1.62 billion tonnes in 2025, up 1.3 percent year on year. (groups: g-q1-c10)
- Australia supplied 56 percent of seaborne iron ore volumes in 2025, and Brazil supplied 24 percent. (groups: g-q1-c12, g-q1-c13)
- Tonne-mile demand grew faster than tonnes in 2025 because of longer Atlantic-to-Pacific routes, with tonne-miles up 2.3 percent. (groups: g-q1-c7)
- The dry bulk fleet grew in 2025 to about 1,020 million dwt, with fleet growth estimated at 2.4 to 3.1 percent. (groups: g-q3-c1)
- The dry bulk orderbook was about 9.8 to 10.2 percent of the fleet. (groups: g-q3-c2)
- Dry bulk deliveries in 2025 were around 30.1 to 31.6 million dwt, with removals or scrapping reported as very low. (groups: g-q3-c3)
- Capesize earnings ranged between 8,000 and 34,000 US dollars per day within 2025. (groups: g-q4-c2)
- Panamax and Supramax earnings averaged close to 12,000 US dollars per day in 2025. (groups: g-q4-c3)

Uncertainties:
- There is a conflict on the 2025 composite dry bulk freight index: one group says it averaged about 1,650 points, while another says it ended 2025 at 1,480 points. (groups: g-q4-c1, g-q4-c4)
- The 2025 index level is only weakly supported at the year-end figure of 1,480 points. (groups: g-q4-c4)
- The 2026 view is not fully settled because one group says rates will be supported by low fleet growth but capped by soft Chinese steel demand. (groups: g-q2-c17)

Follow-up questions:
- How much of 2026 dry bulk demand depends on Chinese steel demand and property construction?
- What is the expected timing and volume impact of the Simandou iron ore project in Guinea?
- How sensitive are dry bulk earnings to a return to Suez routing versus continued Cape of Good Hope diversions?
- Which grain flows between the Americas and Asia are most likely to shift in 2026?
- How exposed are freight rates to bunker fuel prices and port congestion?

Review findings to resolve:
- MAJOR SMOOTHED_CONFLICT on "The composite dry bulk freight index averaged about 1,650 points, roughly 9 percent below 2024": The cited evidence is split: one group supports the 2025 full-year average of about 1,650 points, while another says the index ended 2025 at 1,480 points. The briefing collapses that open conflict into a single figure instead of stating both values and that the conflict remains unresolved.

Write the briefing following the rules you were given.

