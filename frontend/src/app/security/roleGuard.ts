import { inject, Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from "@angular/router";
import { AuthService } from "../auth/service/auth.service";
import { Role } from "../util/role";

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  authService = inject(AuthService);
  router = inject(Router);
    

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const roles = route.data;
    const userProfile = this.authService.getUserProfile();
    
    if (userProfile == null || userProfile == undefined) {
        return this.router.createUrlTree(['/auth']);
    } else {
        return roles["roles"].includes(userProfile.getUserRole()) ? true : this.router.createUrlTree(['/forbidden']);
    }
  }
}