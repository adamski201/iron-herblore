package adamski.app;

import adamski.domain.ItemQuantities;
import adamski.domain.result.AlchemistAmuletOption;
import adamski.domain.result.ChainResult;
import adamski.domain.balance.SecondaryBalance;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Getter
@EqualsAndHashCode
@ToString
public final class HerbloreResult {
    private final ItemQuantities owned;
    private final int currentXp;
    private final List<ChainResult> chainResults;
    private final double totalXp;
    private final SecondaryBalance secondaryBalance;
    private final List<AlchemistAmuletOption> amuletOptions;
    private final double amuletCharges;
    private final Set<Integer> amuletChainRootItemIds;

    public HerbloreResult(ItemQuantities owned, int currentXp, List<ChainResult> chainResults,
                          SecondaryBalance secondaryBalance, List<AlchemistAmuletOption> amuletOptions,
                          double amuletCharges, Set<Integer> amuletChainRootItemIds) {
        this.owned = owned;
        this.currentXp = currentXp;
        this.chainResults = List.copyOf(chainResults);
        this.totalXp = this.chainResults.stream().mapToDouble(ChainResult::getXp).sum();
        this.secondaryBalance = secondaryBalance;
        this.amuletOptions = List.copyOf(amuletOptions);
        this.amuletCharges = amuletCharges;
        this.amuletChainRootItemIds = Set.copyOf(amuletChainRootItemIds);
    }
}
