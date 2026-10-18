import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-nav',
  template: `
    <nav aria-label="Owner area" class="sub">
      <a routerLink="/admin" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" ariaCurrentWhenActive="page">Overview</a>
      <a routerLink="/admin/products" routerLinkActive="active" ariaCurrentWhenActive="page">Catalog</a>
      <a routerLink="/admin/inventory" routerLinkActive="active" ariaCurrentWhenActive="page">Inventory</a>
      <a routerLink="/admin/orders" routerLinkActive="active" ariaCurrentWhenActive="page">Orders</a>
      <a routerLink="/admin/reports" routerLinkActive="active" ariaCurrentWhenActive="page">Reports</a>
    </nav>`,
  styles: [`
    .sub { display: flex; flex-wrap: wrap; gap: 4px 24px; border-bottom: 1px solid var(--border); margin-bottom: 32px; }
    a { display: inline-flex; align-items: center; min-height: 44px; color: var(--ink); text-decoration: none; font-size: .8125rem; font-weight: 600; letter-spacing: .1em; text-transform: uppercase; border-bottom: 2px solid transparent; margin-bottom: -1px; }
    a:hover { color: var(--accent-ink); }
    a.active { color: var(--accent-ink); border-bottom-color: var(--accent); }
  `]
})
export class AdminNavComponent {}
