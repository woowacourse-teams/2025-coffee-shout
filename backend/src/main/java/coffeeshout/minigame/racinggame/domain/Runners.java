package coffeeshout.minigame.racinggame.domain;

import coffeeshout.room.domain.player.Player;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Runners {

    private final List<Runner> runners;

    public Runners(List<Player> players) {
        this.runners = Collections.synchronizedList(new ArrayList<>());
        players.forEach(player -> runners.add(new Runner(player)));
    }

    public void adjustSpeed(Player player, int tapCount, Instant timestamp) {
        Runner runner = findRunnerByPlayer(player);
        runner.adjustSpeed(tapCount, timestamp);
    }

    public void moveAll() {
        runners.forEach(Runner::move);
    }

    public Optional<Player> findWinner() {
        return runners.stream()
                .filter(Runner::isFinished)
                .map(Runner::getPlayer)
                .findFirst();
    }

    public boolean hasWinner() {
        return findWinner().isPresent();
    }

    public List<Player> getRanking() {
        return runners.stream()
                .filter(Runner::isFinished)
                .sorted((r1, r2) -> r1.getFinishTime().compareTo(r2.getFinishTime()))
                .map(Runner::getPlayer)
                .toList();
    }

    public Map<Player, Integer> getPositions() {
        Map<Player, Integer> positions = new LinkedHashMap<>();
        runners.forEach(runner -> positions.put(runner.getPlayer(), runner.getPosition()));
        return positions;
    }

    public Map<Player, Integer> getSpeeds() {
        Map<Player, Integer> speeds = new LinkedHashMap<>();
        runners.forEach(runner -> speeds.put(runner.getPlayer(), runner.getSpeed()));
        return speeds;
    }

    public boolean isAllFinished() {
        return runners.stream().allMatch(Runner::isFinished);
    }

    private Runner findRunnerByPlayer(Player player) {
        return runners.stream()
                .filter(runner -> runner.getPlayer().equals(player))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 플레이어의 러너를 찾을 수 없습니다."));
    }
}
