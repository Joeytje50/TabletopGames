package games.conquest.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IExtendedSequence;
import games.conquest.CQGameState;
import games.conquest.components.Command;
import games.conquest.components.CommandType;
import utilities.Vector2D;
import java.util.logging.Logger;

import java.util.List;

public abstract class CQAction extends AbstractAction implements IExtendedSequence {
    public final int playerId;
    Vector2D highlight;
    Command cmdHighlight;
    private boolean executed = false;

    public CQAction(int pid, Vector2D target) {
        playerId = pid;
        highlight = target;
    }
    public CQAction(int pid, Command cmd, Vector2D target) {
        this(pid, target);
        cmdHighlight = cmd;
    }

    public boolean checkHighlight(Vector2D cmp) {
        return cmp.equals(highlight);
    }
    public boolean checkHighlight(Vector2D cmp, Command cmd) {
        if (cmd == null) return false; // TODO: Is this correct for non-GUI players?
        if (cmd.getCommandType() == CommandType.WindsOfFate) return cmd.equals(cmdHighlight);
        else return cmp.equals(highlight) && cmd.equals(cmdHighlight);
    }

    public abstract boolean canExecute(CQGameState cqgs);

    /**
     * Forward Model delegates to this from {@link core.StandardForwardModel#computeAvailableActions(AbstractGameState)}
     * if this Extended Sequence is currently active.
     *
     * @param gs The current game state
     * @return the list of possible actions for the {@link AbstractGameState#getCurrentPlayer()}.
     * These may be instances of this same class, with more choices between different values for a not-yet filled in parameter.
     */
    @Override
    public List<AbstractAction> _computeAvailableActions(AbstractGameState gs) {
        return ((CQGameState) gs).getAvailableActions();
    }

    @Override
    public int getCurrentPlayer(AbstractGameState state) {
        return playerId;
    }

    @Override
    public void _afterAction(AbstractGameState state, AbstractAction action) {
        executed = true;
    }

    @Override
    public boolean executionComplete(AbstractGameState state) {
        return executed;
    }

    abstract String _toString();
    @Override
    public String toString() {
        if (highlight != null)
            return _toString() + " " + highlight;
        else
            return _toString();
    }

    @Override
    public abstract boolean execute(AbstractGameState gs);

    @Override
    public abstract CQAction copy();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    /**
     * @param gameState - game state provided for context.
     * @return A more descriptive alternative to the toString action, after access to the game state to e.g.
     * retrieve components for which only the ID is stored on the action object, and include the name of those components.
     * Optional.
     */
    @Override
    public String getString(AbstractGameState gameState) {
        return toString();
    }
}