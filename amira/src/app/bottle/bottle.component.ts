import { Component, Input } from '@angular/core';

/**
 * Product picture. Shows the product photo on a white card when there is one,
 * otherwise (or if the photo fails to load) a drawn bottle for the category.
 */
@Component({
  selector: 'app-bottle',
  templateUrl: './bottle.component.html',
  styleUrls: ['./bottle.component.css']
})
export class BottleComponent {
  @Input() category = 'Unisex';
  @Input() label = '';
  @Input() set imageUrl(value: string | null | undefined) {
    this.photo = value || null;
    this.broken = false;
  }

  photo: string | null = null;
  broken = false;

  get key(): string {
    const c = this.category.toLowerCase();
    return c === 'men' ? 'men' : c === 'women' ? 'women' : 'uni';
  }
}
