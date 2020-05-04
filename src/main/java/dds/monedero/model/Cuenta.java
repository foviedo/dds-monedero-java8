package dds.monedero.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Cuenta {

  private BigDecimal saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = new BigDecimal(0);
  }

  public Cuenta(BigDecimal montoInicial) {
    saldo = montoInicial;
  }	

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(BigDecimal cuanto) {
    if (cuanto.compareTo(new BigDecimal(0))<= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3) { //delegar long method
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    new Movimiento(LocalDate.now(), cuanto, true).agregateA(this); //delegar
  }

  public void sacar(BigDecimal cuanto) {
    if (cuanto.compareTo(new BigDecimal(0))<= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo"); //lo valido antes
    }
    if (getSaldo().subtract(cuanto).compareTo(new BigDecimal(0)) < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    BigDecimal montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    BigDecimal limite = (new BigDecimal(1000)).subtract(montoExtraidoHoy);
    if ((cuanto.compareTo(limite)) > 0) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
    new Movimiento(LocalDate.now(), cuanto, false).agregateA(this); //delegar
  }

  public void agregarMovimiento(LocalDate fecha, BigDecimal cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
  }

  public BigDecimal getMontoExtraidoA(LocalDate fecha) {
    return new BigDecimal(getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .map(Movimiento::getMonto).map(BigDecimal::doubleValue).mapToDouble(f-> f.doubleValue())
        .sum()); //long method
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public void setSaldo(BigDecimal saldo) {
    this.saldo = saldo;
  }

}
