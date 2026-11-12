import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  inject,
  provideAppInitializer,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/auth/auth-interceptor';
import { providePrimeNG } from 'primeng/config';
import { routes } from './app.routes';
import { AuthService } from './core/auth/auth-service';
import { ZelaPreset } from './core/theme/zela-preset';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    provideAppInitializer(() => inject(AuthService).iniciar()),
    providePrimeNG({
      translation: {
        emptyMessage: 'Nenhum resultado encontrado',
        emptyFilterMessage: 'Nenhum resultado encontrado',
        noFileChosenMessage: 'Nenhum arquivo selecionado',
      },
      theme: {
        preset: ZelaPreset,
        options: { darkModeSelector: '.app-dark' },
      },
    }),
  ],
};
