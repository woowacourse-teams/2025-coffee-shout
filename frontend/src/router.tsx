import { createBrowserRouter, Outlet } from 'react-router-dom';
import App from './App';
import { EntryMenuPage, EntryNamePage, HomePage } from './pages';
import CardGameProvider from './contexts/CardGame/CardGameProvider';
import { lazy } from 'react';

const LobbyPage = lazy(
  /*webpackChunkName: "lobbyPage"*/ () => import('./features/room/lobby/pages/LobbyPage')
);
const MiniGamePlayPage = lazy(
  () =>
    import(
      /*webpackChunkName: "miniGamePlayPage"*/ './features/miniGame/pages/MiniGamePlayPage/MiniGamePlayPage'
    )
);
const MiniGameReadyPage = lazy(
  () =>
    import(
      /*webpackChunkName: "miniGameReadyPage"*/ './features/miniGame/pages/MiniGameReadyPage/MiniGameReadyPage'
    )
);
const MiniGameResultPage = lazy(
  () =>
    import(
      /*webpackChunkName: "miniGameResultPage"*/ './features/miniGame/pages/MiniGameResultPage/MiniGameResultPage'
    )
);
const NotFoundPage = lazy(
  /*webpackChunkName: "notFoundPage"*/ () => import('./features/notFound/pages/NotFoundPage')
);
const OrderPage = lazy(
  /*webpackChunkName: "orderPage"*/ () => import('./features/room/order/pages/OrderPage')
);
const RoulettePlayPage = lazy(
  () =>
    import(
      /*webpackChunkName: "roulettePlayPage"*/ './features/room/roulette/pages/RoulettePlayPage/RoulettePlayPage'
    )
);
const RouletteResultPage = lazy(
  () =>
    import(
      /*webpackChunkName: "rouletteResultPage"*/ './features/room/roulette/pages/RouletteResultPage/RouletteResultPage'
    )
);
const QRJoinPage = lazy(
  () => import(/*webpackChunkName: "qrJoinPage"*/ './features/join/pages/QRJoinPage')
);

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
