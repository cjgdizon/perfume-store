import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  constructor(router: Router) {
    // after a page change, move focus to the content so keyboard and screen reader users start there
    router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => {
      document.getElementById('main')?.focus({ preventScroll: true });
    });
  }
}
