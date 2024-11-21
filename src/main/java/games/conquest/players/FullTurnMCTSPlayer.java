package games.conquest.players;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.actions.AbstractAction;
import players.PlayerParameters;
import players.mcts.MCTSEnums;
import players.mcts.MCTSParams;
import players.mcts.MCTSPlayer;
import players.mcts.SingleTreeNode;
import java.util.List;

import static players.mcts.MCTSEnums.OpponentTreePolicy.*;

public class FullTurnMCTSPlayer extends MCTSPlayer {
    /**
     * Essentially, this player is a combination between MaxNSearchPlayer with D=1, searchUnit=TURN,
     * followed by MCTSPlayer with the selected parameters. However, this player does not take its initial
     * expansion of the tree for its own turns into account for its budget.
     * <p>
     * This class will build the full tree for a single turn, and then performs MCTS starting from the resulting tree.
     * It takes the exact same parameters as MCTSPlayer, and differs only in the fact that it guarantees every single
     * way to execute the current turn has been visited at least once.
     * It was built for a game which is 99% deterministic (with a single small aspect having randomized effects),
     * so it will determine a set of actions to perform during this turn all in one go, and then execute them all
     * without re-evaluating between.
     */

    public FullTurnMCTSPlayer() {
        this(new MCTSParams());
    }

    public FullTurnMCTSPlayer(PlayerParameters params) {
        super((MCTSParams) params, "FullTurnMCTS");
    }

    @Override
    protected void createRootNode(AbstractGameState gameState) {
        super.createRootNode(gameState);
        root.exploreFullTurn(gameState.getCurrentPlayer(), 0);
        System.out.println(root);
    }

    @Override
    public FullTurnMCTSPlayer copy() {
        FullTurnMCTSPlayer retValue = new FullTurnMCTSPlayer((MCTSParams) getParameters().copy());
        if (getForwardModel() != null)
            retValue.setForwardModel(getForwardModel().copy());
        return retValue;
    }
}
