import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';

/** Signed-in users only; route data { owner: true } also requires the owner role. */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    if (!this.auth.isLoggedIn) {
      return this.router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }
    if (route.data['owner'] && !this.auth.isOwner) {
      return this.router.createUrlTree(['/']);
    }
    return true;
  }
}
