import { Routes } from '@angular/router';
import { AuthComponent } from './auth/component/auth/auth.component';
import { BreakageComponent } from './pages/breakage/breakage.component';
import { UserComponent } from './pages/user/user.component';
import { DepartmentComponent } from './pages/department/department.component';

export const routes: Routes = [
    { path: '', redirectTo: 'auth', pathMatch: 'full' },
    { path: 'auth', component: AuthComponent },
    { path: 'breakage', component: BreakageComponent },
    { path: 'user', component: UserComponent },
    { path: 'department', component: DepartmentComponent }
];
