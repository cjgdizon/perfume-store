import { Component, Input } from '@angular/core';

interface Look {
  cls: string;
  icon: string;
  text?: string;
}

// text and icon always accompany the colour
const LOOKS: Record<string, Look> = {
  Active: { cls: 'ok', icon: 'check', text: 'Available' },
  LowStock: { cls: 'warn', icon: 'alert', text: 'Low stock' },
  Expired: { cls: 'bad', icon: 'alert', text: 'Expired' },
  Discontinued: { cls: 'neutral', icon: 'info', text: 'Discontinued' },
  SoldOut: { cls: 'neutral', icon: 'info', text: 'Sold out' },
  Unavailable: { cls: 'neutral', icon: 'info', text: 'Unavailable right now' },
  Pending: { cls: 'warn', icon: 'clock' },
  Unpaid: { cls: 'warn', icon: 'clock' },
  Paid: { cls: 'ok', icon: 'check' },
  Completed: { cls: 'ok', icon: 'check' },
  Shipped: { cls: 'info', icon: 'truck' },
  Delivered: { cls: 'ok', icon: 'check' },
  Cancelled: { cls: 'bad', icon: 'close' },
  Failed: { cls: 'bad', icon: 'close' },
  Refunded: { cls: 'neutral', icon: 'info' }
};

@Component({
  selector: 'app-status',
  template: `<span class="badge" [ngClass]="look.cls"><app-icon [name]="look.icon" [size]="12"></app-icon>{{ look.text || status }}</span>`
})
export class StatusBadgeComponent {
  @Input() status = '';

  get look(): Look {
    return LOOKS[this.status] ?? { cls: 'neutral', icon: 'info' };
  }
}
