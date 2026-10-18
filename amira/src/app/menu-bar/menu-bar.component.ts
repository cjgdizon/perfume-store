import { Component, EventEmitter, Output } from '@angular/core';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'app-menu-bar',
  templateUrl: './menu-bar.component.html',
  styleUrls: ['./menu-bar.component.css']
})
export class MenuBarComponent {
  /** emitted on every link click so the mobile menu can close */
  @Output() navigate = new EventEmitter<void>();

  constructor(public auth: AuthService) {}
}
