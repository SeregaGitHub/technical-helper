import { Routes } from '@angular/router';
import { AuthComponent } from './auth/component/auth/auth.component';
import { BreakageComponent } from './pages/breakage/breakage.component';
import { UserComponent } from './pages/user/user.component';
import { DepartmentComponent } from './pages/department/department.component';
import { RoleGuard } from './security/roleGuard';
import { Role } from './util/role';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { ForbiddenComponent } from './pages/forbidden/forbidden.component';

export const routes: Routes = [
    { path: '', redirectTo: 'auth', pathMatch: 'full' },
    { path: 'auth', component: AuthComponent },
    { path: 'breakage', component: BreakageComponent },
    { path: 'user', component: UserComponent },
    { path: 'department', component: DepartmentComponent, canActivate: [RoleGuard], data: { role: Role.Admin } },
    { path: 'forbidden', component: ForbiddenComponent },
    { path: '**', component: NotFoundComponent }
];
