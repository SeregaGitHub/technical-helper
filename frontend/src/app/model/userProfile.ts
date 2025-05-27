export class UserProfile {
    name: any;
    role: any;

    getUserName() : String {
        return this.name;
    }

    isAdmin() : boolean {
        return this.role === 'ADMIN';
    }

    isTechnician() : boolean {
        return this.role === 'TECHNICIAN';
    }

    isEmployee() : boolean {
        return this.role === 'EMPLOYEE';
    }
}