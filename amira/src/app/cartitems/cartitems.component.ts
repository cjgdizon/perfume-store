import { Component } from '@angular/core';
import { CartService } from '../service/cart.service';

@Component({
  selector: 'app-cartitems',
  templateUrl: './cartitems.component.html',
  styleUrls: ['./cartitems.component.css']
})
export class CartitemsComponent {
  constructor(public cart: CartService) {}
}
