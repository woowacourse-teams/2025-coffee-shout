import { createBrowserRouter } from 'react-router-dom';
import App from './App';
import {
  EntryMenu,
  EntryName,
  Home,
  Lobby,
  MiniGamePlay,
  MiniGameReady,
  MiniGameResult,
  NotFound,
  Order,
  Roulette,
  RouletteResult,
} from './pages';

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: 'entry',
        children: [
          { path: 'name', element: <EntryName /> },
          { path: 'menu', element: <EntryMenu /> },
        ],
      },
      {
        path: 'room/:roomId',
        children: [
          { path: 'lobby', element: <Lobby /> },
          { path: 'roulette', element: <Roulette /> },
          { path: 'roulette/result', element: <RouletteResult /> },
          { path: 'order', element: <Order /> },
          {
            path: ':miniGameId',
            children: [
              { path: 'ready', element: <MiniGameReady /> },
              { path: 'play', element: <MiniGamePlay /> },
              { path: 'result', element: <MiniGameResult /> },
            ],
          },
        ],
      },
      {
        path: '*',
        element: <NotFound />,
      },
    ],
  },
]);

export default router;
