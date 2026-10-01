package com.assignment.research.bootstrap;

import com.assignment.research.adapter.pricing.CostEstimator;
import com.assignment.research.adapter.pricing.PricingConstants;
import com.assignment.research.adapter.pricing.PricingTable;
import java.io.PrintStream;
import java.nio.file.Files;

public final class CostEstimatorLoader {

    private CostEstimatorLoader() {
    }

    public static CostEstimator load(AppConfig config, PrintStream console) {
        var pricingFile = config.getDataDirectory().resolve(PricingConstants.PRICING_FILE);
        if (!Files.isRegularFile(pricingFile)) {
            console.println(BootstrapConstants.PRICING_MISSING_MESSAGE.formatted(pricingFile.toAbsolutePath()));
            return CostEstimator.free();
        }
        return new CostEstimator(PricingTable.load(pricingFile));
    }
}
