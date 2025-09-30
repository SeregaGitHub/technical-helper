import { Routes } from '@angular/router';
import { AuthComponent } from './auth/components/auth/auth.component';
import { BreakageComponent } from './pages/breakage/breakage.component';
import { UserComponent } from './pages/user/user.component';
import { DepartmentComponent } from './pages/department/department.component';
import { RoleGuard } from './security/role-guard.guard';
import { Role } from './enum/role.enum';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { CurrentBreakageComponent } from './pages/current-breakage/current-breakage.component';


export const routes: Routes = [
    { path: '', redirectTo: 'auth', pathMatch: 'full' },
    { path: 'auth', component: AuthComponent },
    { path: 'breakage', component: BreakageComponent, 
        canActivate: [RoleGuard], data: {
             roles: [Role.Admin, Role.Technician, Role.Employee]
            } },
    { path: 'breakage/:id', component: CurrentBreakageComponent, 
        canActivate: [RoleGuard], data: {
             roles: [Role.Admin, Role.Technician, Role.Employee]
            } },
    { path: 'user', component: UserComponent, 
        canActivate: [RoleGuard], data: { 
            roles: [Role.Admin]
        } },
    { path: 'department', component: DepartmentComponent, 
        canActivate: [RoleGuard], data: {
             roles: [Role.Admin]
            } },
    { path: 'forbidden', component: NotFoundComponent },
    { path: '**', component: NotFoundComponent }
];
