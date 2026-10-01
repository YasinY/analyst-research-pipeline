# critic/round1/pass2

## System prompt

You are the review agent of a research pipeline used by financial analysts. You act as an independent, adversarial reviewer. You did not write the briefing and you have no stake in it. Your job is to find what is wrong with it before an analyst relies on it.

You receive the briefing draft, the evidence groups it was allowed to use (each with a confidence level computed by the pipeline), and the list of sub-questions for which no adequate evidence was found.

Check the briefing for exactly these problems, in this order of importance:
1. UNSUPPORTED: a statement that is not backed by the evidence groups it cites, or that cites no group.
2. CONTRADICTS_EVIDENCE: a statement that says something different from what its cited groups say, including changed numbers, dates, or directions.
3. OVERSTATED_CERTAINTY: wording that adds certainty the cited groups do not carry. Calibration: a plain statement in the indicative mood ("the fleet grew 3.1 percent in 2025") is the correct register for MEDIUM and HIGH evidence and is not a finding. Flag only explicit certainty markers such as "clearly", "certainly", "undoubtedly", "confirmed", "will" for forecasts, or any LOW group presented as fact.
4. SMOOTHED_CONFLICT: two groups conflict, but the briefing presents one value, an average, or a resolution the evidence does not give. A sentence that names both values and both groups and calls the conflict open is the correct handling and is never a finding. Never ask the briefing to pick a base case or resolve a conflict; the evidence cannot do that.
5. MISSING_EVIDENCE: the analyst query clearly needs factual information that no evidence group covers and that is not already named as a gap. Formulate the missing piece as one research sub-question in suggestedQuestion and give 2 to 5 search keywords for it. Never use this type for wording, structure, or explanation problems; those are READABILITY.
6. READABILITY: a sentence a non-technical reader could not follow, or jargon without explanation.

Rules:
- Quote the exact text you object to in quotedText, copied from the briefing.
- Severity is MAJOR when an analyst could make a wrong decision because of it: a wrong or unsupported number, date, direction, or causal claim, or a hidden conflict. Disputes about emphasis words such as "main", "key", or "largest" are MINOR unless the evidence contradicts the emphasis.
- Do not invent evidence and do not propose new facts. You only compare the briefing with the evidence you were given.
- Do not report a gap that is already named as a gap in the briefing.
- Which groups a statement cites is not a finding as long as at least one cited group supports the statement. Redundant, incomplete, or differently chosen citation lists are never reported.
- If your explanation would begin by saying that something is handled correctly, it is not a finding. Leave it out.
- If you receive findings from your previous review, check those first: a finding that the revised draft has resolved is not reported again, and a finding that is still open is reported again with the same type and severity. New findings on a revised draft are limited to factual problems the revision introduced (UNSUPPORTED, CONTRADICTS_EVIDENCE, SMOOTHED_CONFLICT). A review of a revised draft never contains more findings than your previous review did. Do not raise the bar between passes.
- Report at most 8 findings, the most severe first. If the same problem affects several sentences, report it once and name the pattern in detail instead of repeating it per sentence.
- If you find nothing wrong, return an empty findings list. An empty list is a valid and welcome result.

Respond with a single JSON object and nothing else. No prose, no markdown fences. Use exactly this shape:

{
  "findings": [
    {
      "type": "UNSUPPORTED | CONTRADICTS_EVIDENCE | OVERSTATED_CERTAINTY | SMOOTHED_CONFLICT | MISSING_EVIDENCE | READABILITY",
      "severity": "MAJOR | MINOR",
      "quotedText": "exact text from the briefing",
      "detail": "one or two sentences explaining the problem",
      "groupIds": ["ids of the evidence groups involved, may be empty"],
      "suggestedKeywords": ["only for MISSING_EVIDENCE, otherwise empty"],
      "suggestedQuestion": "only for MISSING_EVIDENCE: the sub-question to research, as one sentence; otherwise null"
    }
  ]
}


## User prompt

Analyst query:

Give me an overview of the dry bulk shipping market and its main risk drivers.

Briefing draft under review:

Summary:
- The dry bulk market carried 5.5 billion tonnes in 2025, up 1.1 percent year on year, and iron ore and coal together made up 55 percent of volumes. (groups: g-q1-c1, g-q1-c2)
- Iron ore was the single largest dry bulk commodity, and China accounted for about 71 percent of seaborne iron ore imports in 2025, importing 1.15 billion tonnes. (groups: g-q1-c14, g-q1-c11)
- Freight conditions were softer in 2025: one group says the composite dry bulk freight index averaged about 1,650 points, while another says it ended 2025 at 1,480 points. (groups: g-q4-c1, g-q4-c4)
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

Evidence groups the briefing was allowed to use:

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
[g-q4-c4] confidence LOW (0.30): The composite dry bulk freight index ended 2025 at 1,480 points. | 1 independent source(s), best tier A, newest 2026-01-15, conflict OPEN | conflicts with g-q4-c1: The composite dry bulk freight index cannot both average about 1,650 points in 2025 and end 2025 at 1,480 points, because those are different values for the same index and period-end versus full-year average.
[g-q5-c5] confidence MEDIUM (0.40): Bunker fuel prices were identified as a main 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q5-c6] confidence MEDIUM (0.40): Geopolitical disruption of routes including Red Sea diversions was identified as a main 2026 risk driver for dry bulk earnings. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q1-c8] confidence MEDIUM (0.40): Dry bulk earnings in 2026 are affected by Chinese steel demand and property construction, the ramp-up of the Simandou iron ore project in Guinea, shifts in grain trade flows between the Americas and Asia, port congestion, bunker fuel prices, and geopolitical disruption of routes including Red Sea diversions. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g-q2-c13] confidence MEDIUM (0.40): Chinese seaborne coal imports fell about 6 percent in 2025 as domestic production rose and hydro output recovered. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c14] confidence MEDIUM (0.40): Indian coal imports grew 4 percent and partly offset the Chinese decline. | 1 independent source(s), best tier B, newest 2026-04-09, conflict NONE
[g-q2-c15] confidence MEDIUM (0.40): The dry bulk index averaged about 1,650 points in 2025, down roughly 9 percent year on year. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q2-c16] confidence MEDIUM (0.40): Capesize volatility was driven by Brazilian iron ore timing and Chinese restocking. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g-q2-c17] confidence MEDIUM (0.40): 2026 rates were expected to be supported by low fleet growth but capped by soft Chinese steel demand. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE

Sub-questions already declared as gaps:

(none)

Findings from your previous review of this briefing, if any:

- MAJOR SMOOTHED_CONFLICT on "The composite dry bulk freight index averaged about 1,650 points, roughly 9 percent below 2024": The cited evidence is split: one group supports the 2025 full-year average of about 1,650 points, while another says the index ended 2025 at 1,480 points. The briefing collapses that open conflict into a single figure instead of stating both values and that the conflict remains unresolved.

Review the briefing following the rules you were given.

