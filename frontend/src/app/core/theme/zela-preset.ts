import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

export const ZelaPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: 'var(--zela-navy-50)',
      100: 'var(--zela-navy-100)',
      200: 'color-mix(in srgb, var(--zela-navy-100) 75%, var(--zela-navy-500))',
      300: 'color-mix(in srgb, var(--zela-navy-100) 50%, var(--zela-navy-500))',
      400: 'color-mix(in srgb, var(--zela-navy-100) 25%, var(--zela-navy-500))',
      500: 'var(--zela-navy-500)',
      600: 'color-mix(in srgb, var(--zela-navy-500) 50%, var(--zela-navy-700))',
      700: 'var(--zela-navy-700)',
      800: 'color-mix(in srgb, var(--zela-navy-700) 50%, var(--zela-navy-900))',
      900: 'var(--zela-navy-900)',
      950: 'color-mix(in srgb, var(--zela-navy-900) 60%, black)',
    },
    colorScheme: {
      light: {
        primary: {
          color: '{primary.700}',
          contrastColor: '#ffffff',
          hoverColor: '{primary.800}',
          activeColor: '{primary.900}',
        },
        highlight: {
          background: '{primary.50}',
          focusBackground: '{primary.100}',
          color: '{primary.700}',
          focusColor: '{primary.800}',
        },
        text: {
          color: 'var(--zela-navy-900)',
          hoverColor: 'var(--zela-navy-900)',
          mutedColor: 'var(--zela-ink-600)',
          hoverMutedColor: 'var(--zela-navy-900)',
        },
      },
    },
  },
});
