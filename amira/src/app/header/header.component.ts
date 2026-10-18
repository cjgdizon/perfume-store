import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { CartService } from '../service/cart.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  menuOpen = false;

  constructor(
    public auth: AuthService,
    public cart: CartService,
    private router: Router
  ) {}

  signOut(): void {
    this.auth.logout();
    this.menuOpen = false;
    this.router.navigate(['/']);
  }
}
