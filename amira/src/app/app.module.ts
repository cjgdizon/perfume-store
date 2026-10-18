import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DemoApiInterceptor } from './service/demo-api.interceptor';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { MenuBarComponent } from './menu-bar/menu-bar.component';
import { FooterComponent } from './footer/footer.component';
import { HomeComponent } from './home/home.component';
import { ProductsComponent } from './products/products.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { CartComponent } from './cart/cart.component';
import { CartitemsComponent } from './cartitems/cartitems.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { OrdersComponent } from './orders/orders.component';
import { OrderitemsComponent } from './orderitems/orderitems.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { AccountComponent } from './account/account.component';
import { BottleComponent } from './bottle/bottle.component';
import { IconComponent } from './icon/icon.component';
import { ToastComponent } from './toast/toast.component';
import { AdminDashboardComponent } from './admin/admin-dashboard/admin-dashboard.component';
import { AdminProductsComponent } from './admin/admin-products/admin-products.component';
import { AdminInventoryComponent } from './admin/admin-inventory/admin-inventory.component';
import { AdminOrdersComponent } from './admin/admin-orders/admin-orders.component';
import { AdminReportsComponent } from './admin/admin-reports/admin-reports.component';
import { AuthInterceptor } from './service/auth.interceptor';
import { StatusBadgeComponent } from './status-badge/status-badge.component';
import { AdminNavComponent } from './admin/admin-nav/admin-nav.component';
import { StockSumPipe } from './admin/stock-sum.pipe';
import { ProductCardComponent } from './product-card/product-card.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    MenuBarComponent,
    FooterComponent,
    HomeComponent,
    ProductsComponent,
    ProductDetailComponent,
    CartComponent,
    CartitemsComponent,
    CheckoutComponent,
    OrdersComponent,
    OrderitemsComponent,
    LoginComponent,
    RegisterComponent,
    AccountComponent,
    BottleComponent,
    IconComponent,
    ToastComponent,
    StatusBadgeComponent,
    ProductCardComponent,
    AdminNavComponent,
    StockSumPipe,
    AdminDashboardComponent,
    AdminProductsComponent,
    AdminInventoryComponent,
    AdminOrdersComponent,
    AdminReportsComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    // last in the chain: answers API calls in the browser, only in the "demo" build
    { provide: HTTP_INTERCEPTORS, useClass: DemoApiInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
