package zord;
import robocode.*;

import static robocode.util.Utils.*;
import java.awt.Color;
/**
 * TheZord - a class by (Thiago Cavalcante - 01505909)
 */
public class Zord extends AdvancedRobot {
	final double DIST_COURS = 100, DIST_MOYEN = 225, MOUVIMENTO_LATERAL = 45, MOUVIMENTO_RECUO = 135, VELOCIDADE_MINIMA = 6;
	// BASE = distância curso = 100, média = 225, lateral = 45, recuo = 135, velocidade = 6
	double rand;
	long prevTime = 0;
	int frequenciaAleatoria = 20 + (int) (Math.random() * 30.0);
	int movimentoAleatorio = 1;
	int sentidoRastreamento = 1;
	long tempoRastreamento = 0, tempoEscaneamentoAnterior;
	boolean girandoRadar = false, mirando = false, combateCorpoACorpo = false;
	int corAtual = 0, sentidoCor = 1;
	int estatisticaAleatoria, numAleatorio = 0, totalAleatorio = 0;
	double alvoRelativoY = 0;
	double alvoRelativoX = 0;

	double alvoPreditoRelativoY = 0;
	double alvoPreditoRelativoX = 0;
	double distanciaAlvoPredita = 0;

	double proximoX = 0;
	double proximoY = 0;

	double alvoProximoX = 0;
	double alvoProximoY = 0;
	double direcaoAlvo = 0;
	double velocidadeAlvo = 0;

	double anguloPrecisao = 45;

	public void run() {
		setColors(Color.black,
					Color.gray, 
					Color.black);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while (true) {
			if (!combateCorpoACorpo) {

				if (getRadarTurnRemaining() == 0)
					girandoRadar = false;

				if (!girandoRadar) {
					setTurnRadarRight(sentidoRastreamento * 90);
				}

				distanciaAlvoPredita = 0;

				execute();
			} else {
			}
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		if (!combateCorpoACorpo) {
			movimentoAleatorio();

			mirando = true;
			tempoEscaneamentoAnterior = getTime();

			// Rastreamento

			if (getRadarTurnRemaining() == 0)
				girandoRadar = false;

			direcaoAlvo = e.getHeadingRadians();
			velocidadeAlvo = e.getVelocity();

			alvoRelativoY = Math.cos(normalRelativeAngle(e.getBearingRadians() + getHeadingRadians())) * e.getDistance();
			alvoRelativoX = Math.sin(normalRelativeAngle(e.getBearingRadians() + getHeadingRadians())) * e.getDistance();

			alvoPreditoRelativoY = alvoRelativoY
					+ Math.cos(e.getHeadingRadians()) * e.getVelocity() * distanciaAlvoPredita
							/ Rules.getBulletSpeed(getPotenciaDoTiroPara(e));
			alvoPreditoRelativoX = alvoRelativoX
					+ Math.sin(e.getHeadingRadians()) * e.getVelocity() * distanciaAlvoPredita
							/ Rules.getBulletSpeed(getPotenciaDoTiroPara(e));

			distanciaAlvoPredita = Math.sqrt(alvoPreditoRelativoX * alvoPreditoRelativoX
					+ alvoPreditoRelativoY * alvoPreditoRelativoY);

			proximoX = getX() + Math.sin(getHeadingRadians()) * getVelocity();
			proximoY = getY() + Math.cos(getHeadingRadians()) * getVelocity();

			alvoProximoX = getX() + alvoRelativoX + Math.sin(e.getHeadingRadians()) * e.getVelocity();
			alvoProximoY = getY() + alvoRelativoY + Math.cos(e.getHeadingRadians()) * e.getVelocity();

			anguloPrecisao = Math.abs(Math.atan(getWidth() / e.getDistance()));
			if (anguloPrecisao > Rules.RADAR_TURN_RATE_RADIANS / 2)
				anguloPrecisao = Rules.RADAR_TURN_RATE_RADIANS / 2;
			double giroRadar = normalRelativeAngle(
					Math.atan2(alvoProximoX - proximoX, alvoProximoY - proximoY) + sentidoRastreamento * anguloPrecisao / 2
							- getRadarHeadingRadians());

			double giroCanhao = normalRelativeAngle(
					Math.atan2(alvoPreditoRelativoX, alvoPreditoRelativoY) - getGunHeadingRadians());

			if (!girandoRadar) {
				sentidoRastreamento *= -1;

				setTurnRadarRightRadians(normalRelativeAngle(
						e.getBearingRadians() + getHeadingRadians() - getRadarHeadingRadians() + (anguloPrecisao / 2)
								* sentidoRastreamento));
				girandoRadar = true;
			}

			setTurnGunRightRadians(giroCanhao);

			if (Math.abs(giroCanhao) < anguloPrecisao / 2 && distanciaAlvoPredita != 0)
				setFire(getPotenciaDoTiroPara(e));

			if (getTime() > prevTime + frequenciaAleatoria) {
				estatisticaAleatoria = /* 4 */ (int) (Math.random() * 10.0);
				totalAleatorio += estatisticaAleatoria;
				numAleatorio++;
				prevTime = getTime();
				frequenciaAleatoria = 10 + estatisticaAleatoria;
				movimentoAleatorio *= -1;
			}

			if (getX() < 50)
				moveTo(90);
			else if (getX() > getBattleFieldWidth() - 50)
				moveTo(270);
			else if (getY() < 50)
				moveTo(0);
			else if (getY() > getBattleFieldHeight() - 50)
				moveTo(180);
			else if (e.getDistance() > 200)
				moveTo(e.getBearing() + getHeading() + MOUVIMENTO_LATERAL * movimentoAleatorio);
			else if (e.getDistance() < 100)
				moveTo(e.getBearing() + getHeading() + MOUVIMENTO_LATERAL * movimentoAleatorio + MOUVIMENTO_RECUO);
			else
				moveTo(e.getBearing() + getHeading() + 90 * movimentoAleatorio);

			scan();
		} else {
		}
	}

	/**
	 * onHitByBullet: O que fazer quando o robô é atingido por uma bala
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		if (!combateCorpoACorpo) {
			movimentoAleatorio();
			frequenciaAleatoria += 10;
		} else {
		}
	}

	public void onHitRobot(HitRobotEvent e) {
		if (!combateCorpoACorpo) {
			girandoRadar = true;
			setTurnRadarRight(normalRelativeAngleDegrees(e.getBearing()
					+ (getHeading() - getRadarHeading())));
		}
	}

	public void movimentoAleatorio() {

		if (!combateCorpoACorpo) {
			rand = (int) (Math.random() * Rules.MAX_VELOCITY);
			if (rand < VELOCIDADE_MINIMA)
				rand = Rules.MAX_VELOCITY;
			setMaxVelocity(rand);
		}
	}

	public void moveTo(double angulo) {
		if (!combateCorpoACorpo) {

			if (Math.abs(normalRelativeAngleDegrees(angulo - getHeading())) <= 90) {
				setTurnRight(normalRelativeAngleDegrees(angulo - getHeading()));
				setAhead(1000);
			} else {
				setTurnRight(normalRelativeAngleDegrees(angulo - getHeading() + 180));
				setBack(1000);
			}
		}
	}

	public double getPotenciaDoTiroPara(ScannedRobotEvent e) {
		if (!combateCorpoACorpo) {

			if (e.getDistance() < DIST_COURS/*150*/)
				return Rules.MAX_BULLET_POWER;
			else if (e.getDistance() < DIST_MOYEN/*225*/)
				return 2;
			else
				return 1;
		} else {
			return 0;
		}
	}
}
