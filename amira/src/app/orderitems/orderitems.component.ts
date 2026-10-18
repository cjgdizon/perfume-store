import { Component, Input } from '@angular/core';
import { OrderItem } from '../model/order-item';

/** The lines of one order: product, size, quantity and the price paid at the time. */
@Component({
  selector: 'app-orderitems',
  templateUrl: './orderitems.component.html',
  styleUrls: ['./orderitems.component.css']
})
export class OrderitemsComponent {
  @Input() items: OrderItem[] = [];
}
