import { inject, Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from "@angular/router";
import { AuthService } from "../auth/service/auth.service";
import { Role } from "../util/role";

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  authService = inject(AuthService);
  router = inject(Router);

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    console.log('RoleGuard - start !!!')
    const role = route.data["role"];
    const userProfile = this.authService.getUserProfile();
    console.log('role - ' + role)
    console.log('hasAccess - ' + userProfile)
    //const hasAccess = this.authService.hasRole(role);
    if (userProfile == null || userProfile == undefined) {
        return this.router.createUrlTree(['/auth']);
    } else {
        return role === userProfile.getUserRole() ? true : this.router.createUrlTree(['/auth']);
    }
  }
}