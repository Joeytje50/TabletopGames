package games.conquest.gui;

import cats.kernel.Hash;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.CoreConstants;
import core.Game;
import core.actions.AbstractAction;
import games.conquest.CQGameState;
import games.conquest.CQParameters;
import games.conquest.actions.*;
import games.conquest.components.*;
import games.terraformingmars.gui.TMGUI;
import games.tictactoe.gui.TicTacToeGUIManager;
import gui.AbstractGUIManager;
import gui.GamePanel;
import gui.IScreenHighlight;
import players.human.ActionController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>This class allows the visualisation of the game. The game components (accessible through {@link Game#getGameState()}
 * should be added into {@link JComponent} subclasses (e.g. {@link JLabel},
 * {@link JPanel}, {@link JScrollPane}; or custom subclasses such as those in {@link gui} package).
 * These JComponents should then be added to the <code>`parent`</code> object received in the class constructor.</p>
 *
 * <p>An appropriate layout should be set for the parent GamePanel as well, e.g. {@link BoxLayout} or
 * {@link BorderLayout} or {@link GridBagLayout}.</p>
 *
 * <p>Check the super class for methods that can be overwritten for a more custom look, or
 * {@link TMGUI} for an advanced game visualisation example.</p>
 *
 * <p>A simple implementation example can be found in {@link TicTacToeGUIManager}.</p>
 */
public class CQGUIManager extends AbstractGUIManager {
    CQBoardView boardView;
    CQCommandView[] commandViews;
    GamePanel gamePanel;
    JLabel[] troopInfo;
    JLabel[] commandInfo;

    final static int commandWidth = 50;
    final static int commandHeight = 50;
    private Command commandSelection;

    public CQGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> human) {
        super(parent, game, ac, human);
        gamePanel = parent;
        if (game == null) return;

        CQGameState cqgs = (CQGameState) game.getGameState();
        CQParameters cqp = (CQParameters) cqgs.getGameParameters();
        boardView = new CQBoardView(cqgs.getGridBoard());
        commandViews = new CQCommandView[2];
        commandInfo = new JLabel[5];
        defaultItemSize = boardView.defaultCellSize;

        JPanel commandView = new JPanel(new BorderLayout());
        for (int i=0;i<2;i++) {
            commandViews[i] = new CQCommandView(human.stream().findFirst().orElse(0), cqgs.getCommands(i), false, cqp.dataPath);
            commandViews[i].minCardOffset = commandWidth + 3;
        }
        commandView.add(commandViews[0], BorderLayout.NORTH);
        commandView.add(commandViews[1], BorderLayout.SOUTH);
        commandView.add(addCommandInfoPanel(), BorderLayout.CENTER);
        showCommandInfo(cqgs);

        this.width = Math.max(defaultDisplayWidth, defaultItemSize * cqgs.getGridBoard().getWidth());
        this.height = defaultItemSize * cqgs.getGridBoard().getHeight();

        JPanel infoPanel = createGameStateInfoPanel("Conquest", cqgs, width, defaultInfoPanelHeight);
        troopInfo = new JLabel[11];
        infoPanel.add(addTroopInfoPanel());
        JComponent actionPanel = createActionPanel(new IScreenHighlight[]{boardView}, width, defaultActionPanelHeight, true);

        JPanel centerArea = new JPanel(new GridLayout(1,2));
        centerArea.add(boardView);
        centerArea.add(commandView);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(centerArea, BorderLayout.CENTER);
        gamePanel.add(infoPanel, BorderLayout.NORTH);
        gamePanel.add(actionPanel, BorderLayout.SOUTH);
        gamePanel.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
        gamePanel.revalidate();
        gamePanel.setVisible(true);
        gamePanel.repaint();
    }

    /**
     * Only shows actions for highlighted cell.
     * @param player - current player acting.
     * @param gameState - current game state to be used in updating visuals.
     */
    @Override
    protected void updateActionButtons(AbstractPlayer player, AbstractGameState gameState) {
        CQGameState cqgs = (CQGameState) gameState;
        if (cqgs.getGameStatus() == CoreConstants.GameResult.GAME_ONGOING) {
//            List<AbstractAction> actions = player.getForwardModel().computeAvailableActions(gameState);
            List<AbstractAction> actions = cqgs.getGenericActions(); // TODO: better way to do this, without evading ForwardModel?
            ArrayList<Rectangle> highlight = boardView.getHighlight();

            int start = actions.size();
            if (!highlight.isEmpty()) {
                Troop troop = cqgs.getTroopByRect(highlight.get(0));
                int i = 0;
                for (AbstractAction action : actions) {
                    if (cqgs.canPerformAction(action)) {
                        actionButtons[i].setButtonAction(action, action.toString());
                        actionButtons[i].setVisible(true);
                        i++;
                    }
                }
                while (i < actionButtons.length) {
                    // iterate through the rest of the buttons to ensure they're hidden
                    actionButtons[i++].setVisible(false);
                }
            } else {
                for (ActionButton a: actionButtons) {
                    a.setVisible(false);
                    a.setButtonAction(null, "");
                }
                EndTurn end = new EndTurn();
                actionButtons[0].setButtonAction(end, end.toString());
                actionButtons[0].setVisible(true);
            }
        }
    }

    private JPanel addTroopInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(9,1));
        panel.setPreferredSize(new Dimension(250, defaultInfoPanelHeight+20));
        JPanel cmdIcons = new JPanel(new GridLayout(1, 4));
        for (int i=0; i<troopInfo.length; i++) {
            troopInfo[i] = new JLabel("");
            if (i < troopInfo.length - 4) {
                panel.add(troopInfo[i]);
            } else {
                cmdIcons.add(troopInfo[i]);
            }
        }
        panel.add(cmdIcons);
        troopInfo[0].setHorizontalAlignment(SwingConstants.CENTER);
        troopInfo[0].setFont(troopInfo[0].getFont().deriveFont(16f));
        return panel;
    }

    private void showTroopInfo(CQGameState cqgs, Rectangle r) {
        Troop troop = null;
        if (r != null) {
            troop = cqgs.getTroopByRect(r);
        }
        for (int i=0;i<troopInfo.length;i++)
            if (i != 1)
                troopInfo[i].setVisible(troop != null);
        if (troop == null) {
            troopInfo[1].setText("Select a troop to view details.");
            return;
        }
        TroopType tt = troop.getTroopType();
        troopInfo[0].setText(tt.toString());
        troopInfo[1].setText("Range: " + troop.getRange());
        troopInfo[2].setText("Cost: " + troop.getTroopType().cost);
        troopInfo[3].setText("Damage: " + troop.getDamage() + "/" + tt.damage);
        troopInfo[4].setText("Movement: " + troop.getMovement() + "/" + tt.movement);
        troopInfo[5].setText("Health: " + troop.getHealth() + "/" + tt.health);
        troopInfo[6].setText("Commands applied:");
        int i = 0;
        for (CommandType cmd : troop.getAppliedCommands()) {
            troopInfo[7+i].setIcon(cmd.icon);
            troopInfo[7+i].setVisible(true);
            i++;
        }
        while (i < 4) {
            troopInfo[7+(i++)].setVisible(false);
        }
    }

    public JPanel addCommandInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Directly add the command point counters
        commandInfo[0] = new JLabel("");
        panel.add(commandInfo[0], BorderLayout.NORTH);
        commandInfo[4] = new JLabel("");
        panel.add(commandInfo[4], BorderLayout.SOUTH);
        // Add information block for the selected command separately.
        JPanel subPanel = new JPanel(new GridLayout(3,1));
        subPanel.setPreferredSize(new Dimension(100, 50));
        for (int i=1; i<commandInfo.length - 1; i++) {
            // skip [0] and last element; those were already added separately at the top/bottom
            commandInfo[i] = new JLabel("");
            subPanel.add(commandInfo[i]);
        }
        panel.add(subPanel, BorderLayout.CENTER); // add subPanel in the middle
//        commandInfo[1].setHorizontalAlignment(SwingConstants.CENTER);
        commandInfo[1].setFont(commandInfo[1].getFont().deriveFont(16f));
        return panel;
    }

    public void showCommandInfo(CQGameState cqgs) {
        commandInfo[0].setText("Command Points: " + cqgs.getCommandPoints(0));
        int currentPlayer = cqgs.getCurrentPlayer();
        Command highlight = commandViews[currentPlayer].highlight;
        if (highlight == null) {
            for (int i=1; i<3; i++) {
                commandInfo[i].setText("");
            }
        } else {
            commandInfo[1].setText(highlight.getCommandType().name);
            commandInfo[2].setText("Cooldown: " + highlight.getCooldown() + "/" + highlight.getCommandType().cooldown);
            commandInfo[3].setText("Cost: " + highlight.getCost());
        }
        commandInfo[4].setText("Command Points: " + cqgs.getCommandPoints(1));
    }

    /**
     * Defines how many action button objects will be created and cached for usage if needed. Less is better, but
     * should not be smaller than the number of actions available to players in any game state.
     *
     * @return maximum size of the action space (maximum actions available to a player for any decision point in the game)
     */
    @Override
    public int getMaxActionSpace() {
        // TODO
        return 10;
    }

    /**
     * Updates all GUI elements given current game state and player that is currently acting.
     *
     * @param player    - current player acting.
     * @param gameState - current game state to be used in updating visuals.
     */
    @Override
    protected void _update(AbstractPlayer player, AbstractGameState gameState) {
        CQGameState cqgs = (CQGameState) gameState;
        commandSelection = commandViews[cqgs.getCurrentPlayer()].getHighlight();
        boardView.update(cqgs);
        if (cqgs.getGameStatus() == CoreConstants.GameResult.GAME_ONGOING) {
            ArrayList<Rectangle> highlight = boardView.getHighlight();

            if (!highlight.isEmpty()) {
                Rectangle r = highlight.get(0);
                Troop troop = cqgs.getTroopByRect(r);
                if (troop != null) {
                    cqgs.highlight = troop.getLocation();
                }
                showTroopInfo(cqgs, r); // show highlighted troop
                cqgs.highlight = cqgs.getLocationByRect(r);
            } else {
                showTroopInfo(cqgs, null);
                cqgs.highlight = null;
            }
            cqgs.cmdHighlight = commandSelection;
            showCommandInfo(cqgs);
//            System.out.println("Visible:");
//            System.out.println(cqgs.getCommands(0).getVisibleComponents(1));
        }
    }
}