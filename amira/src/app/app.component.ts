import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { environment } from 'src/environments/environment';
import { resetDemo } from './demo/demo-backend';
import { AuthService } from './service/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  /** true only in the portfolio demo build, which has no real backend */
  demo = environment.demo;

  constructor(router: Router, private auth: AuthService) {
    // after a page change, move focus to the content so keyboard and screen reader users start there
    router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => {
      document.getElementById('main')?.focus({ preventScroll: true });
    });
  }

  /** demo only: throw away this browser's orders and changes and start again from the starter catalog */
  resetDemo(): void {
    resetDemo();
    this.auth.clear();
    window.location.assign('/');
  }
}
