import Layout from '@/layouts/Layout';
import { MiniGameType } from '@/types/miniGame/common';
import MiniGameIntroSlide from '../MiniGameIntroSlide/MiniGameIntroSlide';
import { getGameSlideConfig } from '../../config/gameSlideConfigs';

type Props = {
  gameType: MiniGameType;
};

const GameIntroSlides = ({ gameType }: Props) => {
  const slideConfig = getGameSlideConfig(gameType);

  return (
    <Layout color="point-400">
      <Layout.Content>
        {slideConfig.map((slide, index) => (
          <MiniGameIntroSlide
            key={index}
            textLines={slide.textLines}
            imageSrc={slide.imageSrc}
            className={slide.className}
          />
        ))}
      </Layout.Content>
    </Layout>
  );
};

export default GameIntroSlides;
