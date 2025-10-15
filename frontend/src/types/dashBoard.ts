export type TopWinner = {
  nickname: string;
  winCount: number;
};

export type LowestProbabilityWinner = {
  nicknames: [string];
  probability: number;
};

export type GamePlayCount = {
  gameType: string;
  playCount: number;
};
