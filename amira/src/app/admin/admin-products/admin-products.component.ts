import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Product } from '../../model/product';
import { ProductVariant } from '../../model/product-variant';
import { CATEGORIES, monthYear } from '../../model/product-utils';
import { ProductService } from '../../service/product.service';
import { ToastService } from '../../service/toast.service';

@Component({
  selector: 'app-admin-products',
  templateUrl: './admin-products.component.html',
  styleUrls: ['./admin-products.component.css']
})
export class AdminProductsComponent implements OnInit {
  products: Product[] = [];
  categories = CATEGORIES;
  loading = true;
  failed = false;
  monthYear = monthYear;

  /** the product being edited; id 0 means a new one; null shows the list */
  editing: Product | null = null;
  /** the variant form: null hidden, id 0 adding, otherwise editing that variant */
  variantId: number | null = null;
  saving = false;
  error = '';

  productForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    brand: [''],
    category: ['Unisex', Validators.required],
    description: ['']
  });

  variantForm = this.fb.nonNullable.group({
    size: ['', Validators.required],
    sku: [''],
    price: [0, [Validators.required, Validators.min(0.01)]],
    costPrice: [0, [Validators.required, Validators.min(0)]],
    stockQuantity: [0, [Validators.required, Validators.min(0)]],
    batchNumber: [''],
    manufactureDate: [''],
    expiryDate: ['']
  });

  constructor(private fb: FormBuilder, private productService: ProductService, private toast: ToastService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.productService.getProducts().subscribe({
      next: p => {
        this.products = p;
        this.loading = false;
        this.failed = false;
        if (this.editing && this.editing.id) {
          this.editing = p.find(x => x.id === this.editing!.id) ?? null;
        }
      },
      error: () => {
        this.loading = false;
        this.failed = true;
      }
    });
  }

  startNew(): void {
    this.editing = { id: 0, name: '', brand: '', description: '', category: 'Unisex', variants: [] };
    this.productForm.reset({ name: '', brand: '', category: 'Unisex', description: '' });
    this.variantId = null;
    this.error = '';
  }

  edit(p: Product): void {
    this.editing = p;
    this.productForm.reset({ name: p.name, brand: p.brand ?? '', category: p.category, description: p.description ?? '' });
    this.variantId = null;
    this.error = '';
  }

  back(): void {
    this.editing = null;
    this.variantId = null;
  }

  saveProduct(): void {
    if (!this.editing) {
      return;
    }
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.error = '';
    const body = { ...this.editing, ...this.productForm.getRawValue() };
    const call = this.editing.id ? this.productService.updateProduct(body) : this.productService.createProduct(body);
    call.subscribe({
      next: saved => {
        this.saving = false;
        this.toast.success(this.editing!.id ? 'Perfume saved' : 'Perfume added. Now add its sizes below.');
        this.editing = saved;
        this.load();
      },
      error: err => {
        this.saving = false;
        this.error = ToastService.messageOf(err);
      }
    });
  }

  deleteProduct(p: Product): void {
    if (!confirm(`Delete ${p.name} and all of its sizes? This cannot be undone.`)) {
      return;
    }
    this.productService.deleteProduct(p.id).subscribe({
      next: () => {
        this.toast.success(`${p.name} deleted`);
        this.back();
        this.load();
      },
      error: err => this.toast.error(ToastService.messageOf(err))
    });
  }

  newVariant(): void {
    this.variantId = 0;
    this.variantForm.reset({ size: '', sku: '', price: 0, costPrice: 0, stockQuantity: 0, batchNumber: '', manufactureDate: '', expiryDate: '' });
    this.error = '';
  }

  editVariant(v: ProductVariant): void {
    this.variantId = v.id;
    this.variantForm.reset({
      size: v.size, sku: v.sku ?? '', price: v.price, costPrice: v.costPrice ?? 0, stockQuantity: v.stockQuantity,
      batchNumber: v.batchNumber ?? '', manufactureDate: v.manufactureDate ?? '', expiryDate: v.expiryDate ?? ''
    });
    this.error = '';
  }

  saveVariant(): void {
    if (!this.editing || this.variantId === null) {
      return;
    }
    if (this.variantForm.invalid) {
      this.variantForm.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.error = '';
    const v = this.variantForm.getRawValue();
    // empty date inputs must be sent as null, not ""
    const body = { ...v, manufactureDate: v.manufactureDate || null, expiryDate: v.expiryDate || null, productId: this.editing.id, id: this.variantId };
    const call = this.variantId ? this.productService.updateVariant(body) : this.productService.addVariant(body);
    call.subscribe({
      next: () => {
        this.saving = false;
        this.toast.success('Size saved');
        this.variantId = null;
        this.load();
      },
      error: err => {
        this.saving = false;
        this.error = ToastService.messageOf(err);
      }
    });
  }

  discontinue(v: ProductVariant): void {
    if (!confirm(`Take ${v.size} off sale? Shoppers will no longer see it as available.`)) {
      return;
    }
    this.productService.deactivateVariant(v.id).subscribe({
      next: () => { this.toast.success('Size taken off sale'); this.load(); },
      error: err => this.toast.error(ToastService.messageOf(err))
    });
  }

  deleteVariant(v: ProductVariant): void {
    if (!confirm(`Delete the ${v.size} size? This cannot be undone.`)) {
      return;
    }
    this.productService.deleteVariant(v.id).subscribe({
      next: () => { this.toast.success('Size deleted'); this.load(); },
      error: err => this.toast.error(ToastService.messageOf(err))
    });
  }

  bad(form: 'productForm' | 'variantForm', name: string): boolean {
    const c = (this[form] as any).controls[name];
    return c.invalid && c.touched;
  }
}
