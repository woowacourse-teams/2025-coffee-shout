import { createBrowserRouter } from 'react-router-dom';
import App from './App';
import {
  EntryMenuPage,
  EntryNamePage,
  HomePage,
  LobbyPage,
  MiniGamePlayPage,
  MiniGameReadyPage,
  MiniGameResultPage,
  NotFoundPage,
  OrderPage,
  RoulettePage,
  RouletteResultPage,
} from './pages';

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'entry',
        children: [
          { path: 'name', element: <EntryNamePage /> },
          { path: 'menu', element: <EntryMenuPage /> },
        ],
      },
      {
        path: 'room/:roomId',
        children: [
          { path: 'lobby', element: <LobbyPage /> },
          { path: 'roulette/play', element: <RoulettePage /> },
          { path: 'roulette/result', element: <RouletteResultPage /> },
          { path: 'order', element: <OrderPage /> },
          {
            path: ':miniGameId',
            children: [
              { path: 'ready', element: <MiniGameReadyPage /> },
              { path: 'play', element: <MiniGamePlayPage /> },
              { path: 'result', element: <MiniGameResultPage /> },
            ],
          },
        ],
      },
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
]);

export default router;
