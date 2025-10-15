export type TopWinner = {
  nickname: string;
  winCount: number;
};

export type LowestProbabilityWinner = {
  nickname: [string];
  probability: number;
};

export type GamePlayCount = {
  gameType: string;
  playCount: number;
};
