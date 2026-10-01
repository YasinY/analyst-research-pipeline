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

The analyst is asking for a factual overview of the dry bulk shipping market and the main factors that drive its risks and volatility.

Evidence eligible for key facts:

[g-q1-c6] confidence MEDIUM (0.60): The dry bulk orderbook was around 10.2 percent of the fleet and was concentrated in the Panamax and Supramax segments at year end. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g-q1-c7] confidence HIGH (0.75): China took roughly 70 to 71 percent of seaborne iron ore imports in 2025, including 1.15 billion tonnes, while iron ore remained the largest dry bulk commodity. | 2 independent source(s), best tier A, newest 2026-04-09, conflict NONE
[g-q1-c8] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025 and this weighed on Panamax demand in the Pacific. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q1-c10] confidence MEDIUM (0.40): Indian coal imports grew 4 percent in 2025. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q1-c11] confidence HIGH (0.75): The composite dry bulk freight index averaged about 1,650 points in 2025. | 2 independent source(s), best tier A, newest 2026-01-22, conflict NONE
[g-q1-c12] confidence MEDIUM (0.40): One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q1-c14] confidence MEDIUM (0.60): Capesize earnings were highly volatile in 2025, swinging between 8,000 and 34,000 US dollars per day. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q1-c15] confidence MEDIUM (0.60): Panamax and Supramax earnings averaged close to 12,000 US dollars per day and were steadier than Capesize earnings. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g-q2-c4] confidence MEDIUM (0.60): Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025, with iron ore and coal together making up 55 percent of volumes and grain and soybeans 10 percent. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q2-c7] confidence MEDIUM (0.60): Grain trade shifted toward South American exporters after a weak North American harvest. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g-q2-c12] confidence HIGH (0.75): Tonne-mile demand grew faster than tonnes because of longer Atlantic-to-Pacific routes, including a 2.3 percent rise in 2025. | 2 independent source(s), best tier A, newest 2026-05-20, conflict NONE
[g-q2-c9] confidence MEDIUM (0.40): Chinese steel demand and property construction are identified as main risk drivers for dry bulk earnings in 2026. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q2-c10] confidence MEDIUM (0.40): The ramp-up of the Simandou iron ore project in Guinea is identified as a main risk driver because it lengthens average haul distances. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q2-c11] confidence MEDIUM (0.40): Shifts in grain trade flows between the Americas and Asia are identified as a main risk driver for dry bulk earnings in 2026. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q3-c14] confidence MEDIUM (0.55): Industry participants expected deliveries to stay elevated in 2026 before easing in 2027, and the shipbroker expected net fleet growth of 2.0 to 2.5 percent in 2026. | 2 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g-q4-c6] confidence MEDIUM (0.40): Continued diversions around the Cape of Good Hope added 1 to 2 percent to dry bulk tonne-mile demand in 2025, and a return to Suez routing would release the equivalent of roughly 2 percent of fleet capacity. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE
[g-q4-c9] confidence MEDIUM (0.40): The main risk drivers for dry bulk earnings in 2026 include Chinese steel demand and property construction, the Simandou ramp-up, grain flow shifts, port congestion, bunker fuel prices, and geopolitical disruption of routes including Red Sea diversions. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q2-c15] confidence MEDIUM (0.40): Capesize volatility was driven by Brazilian iron ore timing and Chinese restocking. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q2-c16] confidence MEDIUM (0.40): 2026 rates were expected to be capped by soft Chinese steel demand. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE

Weak evidence (may only appear in uncertainties):

[g-q1-c1] confidence LOW (0.30): The world dry bulk fleet counted by the bureau covers vessels above 10,000 dwt and grew in 2025 to about 1,020 million dwt, with Capesize at 39 percent, Panamax at 25 percent, and Supramax and Handysize making up the rest. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c1: The dry bulk fleet growth rate in 2025 is given as 3.1 percent in g1 but 2.4 percent in g16.
[g-q3-c1] confidence LOW (0.10): The shipbroker said the dry bulk fleet expanded by 2.4 percent in 2025, with deliveries of 30.1 million dwt, removals of 5.5 million dwt, and an orderbook of 9.8 percent of the fleet. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g-q1-c1, g-q3-c11: The dry bulk fleet growth rate in 2025 is given as 3.1 percent in g1 but 2.4 percent in g16.
[g-q3-c11] confidence LOW (0.30): The bureau said dry bulk fleet deliveries reached 31.6 million dwt in 2025 and demolition stayed low at 3.9 million dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g-q3-c1: 2025 deliveries are given as 30.1 million dwt in g16 but 31.6 million dwt in g17, and removals/demolition are given as 5.5 million dwt in g16 versus 3.9 million dwt in g17.

Gaps (sub-questions without adequate evidence):

(none)

Revision context:

Previous draft:
Summary:
- The dry bulk market grew in 2025, with seaborne trade at 5.5 billion tonnes and the composite freight index averaging about 1,650 points. Tonne-mile demand rose faster than tonnes, helped by longer Atlantic-to-Pacific routes and continued diversions around the Cape of Good Hope. The main risk drivers for 2026 are Chinese steel demand and property construction, the Simandou ramp-up, grain flow shifts, port congestion, bunker fuel prices, and route disruption such as Red Sea diversions. Freight earnings were uneven across vessel types, with Capesize far more volatile than Panamax and Supramax. (groups: g-q2-c4, g-q2-c12, g-q4-c6, g-q4-c9, g-q1-c11, g-q1-c14, g-q1-c15, g-q2-c9, g-q2-c10, g-q2-c11)

Key facts:
- Total seaborne dry bulk trade grew 1.1 percent to 5.5 billion tonnes in 2025. (groups: g-q2-c4)
- Iron ore and coal together made up 55 percent of dry bulk volumes, while grain and soybeans made up 10 percent. (groups: g-q2-c4)
- China took roughly 70 to 71 percent of seaborne iron ore imports in 2025, including 1.15 billion tonnes. (groups: g-q1-c7)
- Iron ore remained the largest dry bulk commodity. (groups: g-q1-c7)
- Chinese seaborne coal imports fell about 6 percent in 2025, while Indian coal imports grew 4 percent. (groups: g-q1-c8, g-q1-c10)
- The composite dry bulk freight index averaged about 1,650 points in 2025. (groups: g-q1-c11)
- One-year time charter rates for a modern Kamsarmax averaged 13,500 US dollars per day. (groups: g-q1-c12)
- Capesize earnings were highly volatile in 2025, swinging between 8,000 and 34,000 US dollars per day. (groups: g-q1-c14)
- Panamax and Supramax earnings averaged close to 12,000 US dollars per day and were steadier than Capesize earnings. (groups: g-q1-c15)
- Tonne-mile demand grew faster than tonnes, including a 2.3 percent rise in 2025. (groups: g-q2-c12)

Uncertainties:
- There is an open conflict on 2025 fleet growth and fleet size. One group says the fleet grew by 3.1 percent and was about 1,020 million dwt, while another says it grew by 2.4 percent. (groups: g-q1-c1, g-q3-c1)
- There is an open conflict on 2025 deliveries and removals. One group says deliveries were 30.1 million dwt and removals were 5.5 million dwt, while another says deliveries were 31.6 million dwt and demolition was 3.9 million dwt. (groups: g-q3-c1, g-q3-c11)
- There is an open conflict on the orderbook. One group says it was around 10.2 percent of the fleet and concentrated in the Panamax and Supramax segments, while another says it was 9.8 percent of the fleet. (groups: g-q1-c6, g-q3-c1)
- The evidence for 2026 fleet growth is limited. One group says deliveries are expected to stay elevated in 2026 before easing in 2027, and net fleet growth may be 2.0 to 2.5 percent in 2026. (groups: g-q3-c14)
- No adequate evidence was found for any other 2026 risk drivers beyond the named factors in the evidence. (groups: )

Follow-up questions:
- How much of 2026 dry bulk earnings risk comes from Chinese steel demand versus property construction?
- What is the expected timing and scale of the Simandou iron ore ramp-up, and how much could it change voyage distances?
- How exposed are Panamax and Supramax earnings to grain flow shifts between the Americas and Asia?
- What would a return to Suez routing mean for tonne-mile demand and fleet capacity?
- How large is the current orderbook by vessel segment after reconciling the conflicting figures?

Review findings to resolve:
- MAJOR UNSUPPORTED on "Freight earnings were uneven across vessel types, with Capesize far more volatile than Panamax and Supramax.": The cited groups support the volatility comparison, but the phrase "Freight earnings were uneven across vessel types" is too general and not directly stated as such. More importantly, this sentence also relies on a broad synthesis across multiple groups without any single group explicitly making that overall claim.
- MAJOR UNSUPPORTED on "The main risk drivers for 2026 are Chinese steel demand and property construction, the Simandou ramp-up, grain flow shifts, port congestion, bunker fuel prices, and route disruption such as Red Sea diversions.": The cited evidence names these as main risk drivers, but it does not support the statement as an exhaustive list. In particular, saying "the main risk drivers for 2026 are" implies completeness that the evidence does not establish.
- MINOR OVERSTATED_CERTAINTY on "The main risk drivers for 2026 are Chinese steel demand and property construction, the Simandou ramp-up, grain flow shifts, port congestion, bunker fuel prices, and route disruption such as Red Sea diversions.": The cited groups describe these as risk drivers, but the wording "are" presents them as settled and exhaustive rather than as identified drivers. The evidence supports a tentative list of key drivers, not certainty about completeness.
- MAJOR SMOOTHED_CONFLICT on "There is an open conflict on 2025 fleet growth and fleet size. One group says the fleet grew by 3.1 percent and was about 1,020 million dwt, while another says it grew by 2.4 percent.": This sentence collapses the conflict into a single narrative and omits that the cited LOW group also gives deliveries, removals, and fleet size in the same claim set. It does flag the conflict, but the wording still smooths over the uncertainty by presenting one side as a coherent fact pattern rather than keeping the values clearly separated.

Write the briefing following the rules you were given.

