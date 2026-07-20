import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), AuthService],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('stores session on login', () => {
    service.login({ username: 'admin', password: 'x' }).subscribe();
    const req = http.expectOne(`${environment.apiBaseUrl}/auth/login`);
    req.flush({
      accessToken: 'tok',
      username: 'admin',
      role: 'ADMIN',
    });
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.role()).toBe('ADMIN');
    expect(service.token()).toBe('tok');
  });
});
