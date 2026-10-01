# critic/round1/pass1

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
- The dry bulk market was softer in 2025, with the composite freight index averaging about 1,650 points, about 9 percent below 2024, and ending the year at 1,480 points. Freight and tonne-mile demand were supported by longer routes, especially Red Sea and Suez diversions, which added about 1 to 2 percent to dry bulk tonne-mile demand. Main risk drivers were Chinese steel demand and property construction, the Simandou iron ore ramp-up, grain trade shifts, port congestion, bunker fuel prices, and geopolitical route disruption. (groups: g17, g3, g5)
- The market is still heavily driven by iron ore and coal, which together made up 55 percent of seaborne dry bulk trade volumes in 2025, while iron ore remained the single largest commodity. China accounted for 71 percent of seaborne iron ore imports, so Chinese demand is a major swing factor. Grain and soybeans also mattered, at 10 percent of seaborne dry bulk trade volumes, and grain trade moved toward South American exporters after a weak North American harvest. (groups: g6, g8, g7, g9)
- Fleet supply was still growing, with about 31.6 million dwt of new ships delivered in 2025 and only about 3.9 million dwt demolished. The 2026 fleet growth outlook was about 2.0 to 2.5 percent, while shipyard slots remained tight and newbuilding prices high. A return to Suez routing would release about 2 percent of fleet capacity and was described as the largest single downside risk to freight rates in 2026. (groups: g13, g15, g20, g3)

Key facts:
- The composite dry bulk freight index averaged about 1,650 points in 2025, about 9 percent below 2024, and ended the year at 1,480 points. (groups: g17)
- Tonne-mile demand grew 2.3 percent in 2025. (groups: g4)
- Continued Red Sea and Suez diversions in 2025 added about 1 to 2 percent to dry bulk tonne-mile demand. (groups: g3)
- Iron ore and coal together made up 55 percent of seaborne dry bulk trade volumes in 2025, and iron ore remained the single largest dry bulk commodity. (groups: g6)
- China accounted for 71 percent of seaborne iron ore imports in 2025, with seaborne iron ore trade reaching 1.62 billion tonnes and Australia and Brazil supplying 56 percent and 24 percent respectively. (groups: g8)
- Grain and soybeans made up 10 percent of seaborne dry bulk trade volumes in 2025. (groups: g7)
- Grain trade shifted toward South American exporters after a weak North American harvest, and average laden voyage distance reached 5,200 nautical miles. (groups: g9)
- Deliveries of new dry bulk ships were about 31.6 million dwt in 2025, and demolitions were about 3.9 million dwt. (groups: g13)
- The dry bulk fleet net growth outlook for 2026 was about 2.0 to 2.5 percent, and shipyard slots remained tight with newbuilding prices high. (groups: g15)
- A return to Suez routing was described as the largest single downside risk to freight rates in 2026. (groups: g20)

Uncertainties:
- There is an open conflict on 2025 dry bulk fleet growth: g1 says 3.1 percent, g4 implies 2.3 percent, and g14 says 2.4 percent. (groups: g1, g4, g14)
- There is an open conflict on the year-end orderbook share: g2 says 10.2 percent of the fleet, while g14 says 9.8 percent. (groups: g2, g14)
- The 2025 dry bulk fleet size is weakly supported and conflicted: g1 gives about 1,020 million dwt, but it conflicts with other fleet growth estimates. (groups: g1, g14)
- The dry bulk orderbook at year end is weakly supported and conflicted: g2 says about 10.2 percent of the fleet, but it conflicts with another estimate. (groups: g2, g14)

Follow-up questions:
- How much of 2026 freight risk depends on Red Sea and Suez routing staying disrupted?
- What is the latest outlook for Chinese steel demand and property construction?
- How much new dry bulk supply is due to hit the market in 2026 by vessel segment?
- Which of the listed risk drivers is most important for earnings in 2026?
- How exposed are Panamax and Supramax earnings to grain trade shifts and bunker fuel prices?

Evidence groups the briefing was allowed to use:

[g1] confidence LOW (0.30): The world dry bulk fleet in 2025 was about 1,020 million dwt, counted as vessels above 10,000 dwt, with Capesize vessels making up 39 percent of capacity, Panamax 25 percent, and Supramax and Handysize the remainder. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g14: The 2025 dry bulk fleet growth rate is given as 3.1 percent in g1 and 2.4 percent in g14.
[g2] confidence LOW (0.30): The dry bulk orderbook at year end was about 10.2 percent of the fleet and was concentrated in the Panamax and Supramax segments. | 1 independent source(s), best tier A, newest 2026-02-10, conflict OPEN | conflicts with g14: The year-end orderbook share is given as 10.2 percent of the fleet in g2 and 9.8 percent in g14.
[g3] confidence HIGH (0.75): Continued Red Sea and Suez diversions in 2025 added about 1 to 2 percent to dry bulk tonne-mile demand, and a return to Suez routing would release about 2 percent of fleet capacity. | 2 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g4] confidence MEDIUM (0.45): Tonne-mile demand grew 2.3 percent in 2025. | 2 independent source(s), best tier A, newest 2026-08-03, conflict OPEN | conflicts with g14: The 2025 fleet growth in g4 is 2.3 percent, while g14 says the dry bulk fleet grew 2.4 percent.
[g5] confidence MEDIUM (0.40): Chinese steel demand and property construction, the ramp-up of the Simandou iron ore project in Guinea, shifts in grain trade flows between the Americas and Asia, port congestion, bunker fuel prices, and geopolitical disruption of routes including Red Sea diversions were identified as major risk drivers for dry bulk earnings in 2026. | 1 independent source(s), best tier B, newest 2026-05-20, conflict NONE
[g6] confidence HIGH (0.75): Iron ore and coal together made up 55 percent of seaborne dry bulk trade volumes in 2025, and iron ore remained the single largest dry bulk commodity. | 2 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g7] confidence MEDIUM (0.60): Grain and soybeans made up 10 percent of seaborne dry bulk trade volumes in 2025. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g8] confidence HIGH (0.75): China accounted for 71 percent of seaborne iron ore imports in 2025, with seaborne iron ore trade reaching 1.62 billion tonnes and Australia and Brazil supplying 56 percent and 24 percent respectively. | 2 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g9] confidence HIGH (0.75): Grain trade shifted toward South American exporters after a weak North American harvest, and average laden voyage distance reached 5,200 nautical miles. | 2 independent source(s), best tier A, newest 2026-08-03, conflict NONE
[g10] confidence MEDIUM (0.60): Tonne-mile demand grew faster than tonnes carried because of longer Atlantic to Pacific routes. | 1 independent source(s), best tier A, newest 2026-03-05, conflict NONE
[g11] confidence LOW (0.40): Chinese seaborne coal imports rose about 5 percent per year between 2016 and 2018. | 1 independent source(s), best tier A, newest 2019-06-14, conflict NONE
[g12] confidence LOW (0.40): Coal remained the main driver of Panamax demand in the Pacific basin, and Indian coal imports were projected to rise steadily with power generation growth. | 1 independent source(s), best tier A, newest 2019-06-14, conflict NONE
[g13] confidence MEDIUM (0.60): Deliveries of new dry bulk ships were about 31.6 million dwt in 2025, and demolitions were about 3.9 million dwt. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g14] confidence LOW (0.10): Broader estimates put 2025 dry bulk fleet growth at 2.4 percent, with deliveries of 30.1 million dwt and removals of 5.5 million dwt. | 1 independent source(s), best tier B, newest 2026-03-18, conflict OPEN | conflicts with g1, g2, g4: The 2025 dry bulk fleet growth rate is given as 3.1 percent in g1 and 2.4 percent in g14.
[g15] confidence MEDIUM (0.55): The dry bulk fleet net growth outlook for 2026 was about 2.0 to 2.5 percent, and shipyard slots remained tight with newbuilding prices high. | 2 independent source(s), best tier B, newest 2026-03-18, conflict NONE
[g16] confidence MEDIUM (0.60): Earnings remained above scrapping thresholds in 2025. | 1 independent source(s), best tier A, newest 2026-02-10, conflict NONE
[g17] confidence HIGH (0.75): The composite dry bulk freight index averaged about 1,650 points in 2025, about 9 percent below 2024, and ended the year at 1,480 points. | 2 independent source(s), best tier A, newest 2026-01-22, conflict NONE
[g18] confidence MEDIUM (0.40): One-year time charter rates for a modern Kamsarmax averaged about 13,500 US dollars per day in 2025. | 1 independent source(s), best tier B, newest 2026-01-22, conflict NONE
[g19] confidence MEDIUM (0.60): Capesize earnings were the most volatile segment in 2025, swinging between 8,000 and 34,000 US dollars per day, while Panamax and Supramax earnings were steadier and averaged close to 12,000 US dollars per day. | 1 independent source(s), best tier A, newest 2026-01-15, conflict NONE
[g20] confidence MEDIUM (0.40): A return to Suez routing was described as the largest single downside risk to freight rates in 2026. | 1 independent source(s), best tier B, newest 2026-07-11, conflict NONE

Sub-questions already declared as gaps:

(none)

Review the briefing following the rules you were given.

