import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ProductsComponent } from './products/products.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { CartComponent } from './cart/cart.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { OrdersComponent } from './orders/orders.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { AccountComponent } from './account/account.component';
import { AdminDashboardComponent } from './admin/admin-dashboard/admin-dashboard.component';
import { AdminProductsComponent } from './admin/admin-products/admin-products.component';
import { AdminInventoryComponent } from './admin/admin-inventory/admin-inventory.component';
import { AdminOrdersComponent } from './admin/admin-orders/admin-orders.component';
import { AdminReportsComponent } from './admin/admin-reports/admin-reports.component';
import { AuthGuard } from './service/auth.guard';

const routes: Routes = [
  { path: '', component: HomeComponent, title: 'AMIRA | Perfumes online' },
  { path: 'products', component: ProductsComponent, title: 'Perfumes | AMIRA' },
  { path: 'products/:id', component: ProductDetailComponent, title: 'Perfume | AMIRA' },
  { path: 'cart', component: CartComponent, title: 'Your cart | AMIRA' },
  { path: 'checkout', component: CheckoutComponent, canActivate: [AuthGuard], title: 'Checkout | AMIRA' },
  { path: 'orders', component: OrdersComponent, canActivate: [AuthGuard], title: 'Your orders | AMIRA' },
  { path: 'account', component: AccountComponent, canActivate: [AuthGuard], title: 'Your account | AMIRA' },
  { path: 'login', component: LoginComponent, title: 'Sign in | AMIRA' },
  { path: 'register', component: RegisterComponent, title: 'Create account | AMIRA' },
  { path: 'forgot-password', component: ForgotPasswordComponent, title: 'Forgot password | AMIRA' },
  { path: 'reset-password', component: ResetPasswordComponent, title: 'Choose a new password | AMIRA' },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [AuthGuard], data: { owner: true }, title: 'Owner dashboard | AMIRA' },
  { path: 'admin/products', component: AdminProductsComponent, canActivate: [AuthGuard], data: { owner: true }, title: 'Catalog | AMIRA' },
  { path: 'admin/inventory', component: AdminInventoryComponent, canActivate: [AuthGuard], data: { owner: true }, title: 'Inventory | AMIRA' },
  { path: 'admin/orders', component: AdminOrdersComponent, canActivate: [AuthGuard], data: { owner: true }, title: 'Orders | AMIRA' },
  { path: 'admin/reports', component: AdminReportsComponent, canActivate: [AuthGuard], data: { owner: true }, title: 'Reports | AMIRA' },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
