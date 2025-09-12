import { createBrowserRouter, Outlet } from 'react-router-dom';
import App from './App';
import { EntryMenuPage, EntryNamePage, HomePage } from './pages';
import CardGameProvider from './contexts/CardGame/CardGameProvider';
import { lazy } from 'react';

const LobbyPage = lazy(() => import('./features/room/lobby/pages/LobbyPage'));
const MiniGamePlayPage = lazy(
  () => import('./features/miniGame/pages/MiniGamePlayPage/MiniGamePlayPage')
);
const MiniGameReadyPage = lazy(
  () => import('./features/miniGame/pages/MiniGameReady/MiniGameReadyPage')
);
const MiniGameResultPage = lazy(
  () => import('./features/miniGame/pages/MiniGameResultPage/MiniGameResultPage')
);
const NotFoundPage = lazy(() => import('./features/notFound/pages/NotFoundPage'));
const OrderPage = lazy(() => import('./features/room/order/pages/OrderPage'));
const RoulettePlayPage = lazy(
  () => import('./features/room/roulette/pages/RoulettePlayPage/RoulettePlayPage')
);
const RouletteResultPage = lazy(
  () => import('./features/room/roulette/pages/RouletteResultPage/RouletteResultPage')
);
const QRJoinPage = lazy(() => import('./features/join/pages/QRJoinPage'));

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
        path: 'room/:joinCode',
        children: [
          { path: 'lobby', element: <LobbyPage /> },
          { path: 'roulette/play', element: <RoulettePlayPage /> },
          { path: 'roulette/result', element: <RouletteResultPage /> },
          { path: 'order', element: <OrderPage /> },
          {
            path: ':miniGameType',
            element: (
              <CardGameProvider>
                <Outlet />
              </CardGameProvider>
            ),
            children: [
              { path: 'ready', element: <MiniGameReadyPage /> },
              { path: 'play', element: <MiniGamePlayPage /> },
              { path: 'result', element: <MiniGameResultPage /> },
            ],
          },
        ],
      },
      {
        path: 'join/:joinCode',
        element: <QRJoinPage />,
      },
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
]);

export default router;
