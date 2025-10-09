package coffeeshout.racinggame.domain;

import coffeeshout.room.domain.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Runners {

    private final List<Runner> runners;

    public Runners(List<Player> players) {
        this.runners = Collections.synchronizedList(new ArrayList<>());
        players.forEach(player -> runners.add(new Runner(player)));
    }

    public void adjustSpeed(Player player, int tapCount) {
        final Runner runner = findRunnerByPlayer(player);
        runner.adjustSpeed(tapCount);
    }

    public void moveAll() {
        runners.forEach(Runner::move);
    }


    public Optional<Runner> findWinner() {
        return runners.stream()
                .filter(Runner::isFinished)
                .max(Comparator.comparing(Runner::getFinishTime));
    }

    public boolean hasWinner() {
        return findWinner().isPresent();
    }

    public Map<Runner, Integer> getPositions() {
        Map<Runner, Integer> positions = new LinkedHashMap<>();
        runners.forEach(runner -> positions.put(runner, runner.getPosition()));
        return positions;
    }

    public Map<Runner, Integer> getSpeeds() {
        Map<Runner, Integer> speeds = new LinkedHashMap<>();
        runners.forEach(runner -> speeds.put(runner, runner.getSpeed()));
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

    public void initialSpeed() {
        runners.forEach(Runner::firstMoveSpeed);
    }

    public Stream<Runner> stream() {
        return runners.stream();
    }
}
