export type RacingGameState = 'DESCRIPTION' | 'PREPARE' | 'PLAYING' | 'DONE';

export type RacingGameData = {
  distance: {
    start: number;
    end: number;
  };
  players: Array<{
    playerName: string;
    position: number;
    speed: number;
  }>;
};
