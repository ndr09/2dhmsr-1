/*
 * Copyright (C) 2020 Eric Medvet <eric.medvet@gmail.com> (as Eric Medvet <eric.medvet@gmail.com>)
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.erallab.hmsrobots.core.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.units.erallab.hmsrobots.core.objects.immutable.Immutable;
import it.units.erallab.hmsrobots.core.sensors.ReadingAugmenter;
import it.units.erallab.hmsrobots.core.sensors.Sensor;
import it.units.erallab.hmsrobots.core.sensors.immutable.SensorReading;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class SensingVoxel extends ControllableVoxel {

  @JsonProperty
  private final List<Sensor> sensors;

  @JsonCreator
  public SensingVoxel(
      @JsonProperty("sideLength") double sideLength,
      @JsonProperty("massSideLengthRatio") double massSideLengthRatio,
      @JsonProperty("springF") double springF,
      @JsonProperty("springD") double springD,
      @JsonProperty("massLinearDamping") double massLinearDamping,
      @JsonProperty("massAngularDamping") double massAngularDamping,
      @JsonProperty("friction") double friction,
      @JsonProperty("restitution") double restitution,
      @JsonProperty("mass") double mass,
      @JsonProperty("limitContractionFlag") boolean limitContractionFlag,
      @JsonProperty("massCollisionFlag") boolean massCollisionFlag,
      @JsonProperty("areaRatioMaxDelta") double areaRatioMaxDelta,
      @JsonProperty("springScaffoldings") EnumSet<SpringScaffolding> springScaffoldings,
      @JsonProperty("maxForce") double maxForce,
      @JsonProperty("forceMethod") ForceMethod forceMethod,
      @JsonProperty("sensors") List<Sensor> sensors
  ) {
    super(sideLength, massSideLengthRatio, springF, springD, massLinearDamping, massAngularDamping, friction, restitution, mass, limitContractionFlag, massCollisionFlag, areaRatioMaxDelta, springScaffoldings, maxForce, forceMethod);
    this.sensors = sensors;
  }

  public SensingVoxel(double maxForce, ForceMethod forceMethod, List<Sensor> sensors) {
    super(maxForce, forceMethod);
    this.sensors = sensors;
  }

  public SensingVoxel(List<Sensor> sensors) {
    this.sensors = sensors;
  }

  protected List<Pair<Sensor, double[]>> lastReadings = List.of();

  public List<Pair<Sensor, double[]>> getLastReadings() {
    return lastReadings;
  }

  @Override
  public void act(double t) {
    super.act(t);
    lastReadings = sensors.stream()
        .map(s -> Pair.of(s, s.sense(this, t)))
        .collect(Collectors.toList());
  }

  public List<Sensor> getSensors() {
    return sensors;
  }

  @Override
  public Immutable immutable() {
    it.units.erallab.hmsrobots.core.objects.immutable.ControllableVoxel immutable = (it.units.erallab.hmsrobots.core.objects.immutable.ControllableVoxel) super.immutable();
    //add sensor readings
    int nOfSensors = lastReadings.size();
    for (int i = 0; i < nOfSensors; i++) {
      Pair<Sensor, double[]> pair = lastReadings.get(i);
      Sensor sensor = pair.getKey();
      SensorReading reading = new SensorReading(pair.getValue(), sensor.domains(), i, nOfSensors);
      if (sensor instanceof ReadingAugmenter) {
        reading = ((ReadingAugmenter) sensor).augment(reading, this);
      }
      immutable.getChildren().add(reading);
    }
    return immutable;
  }

  @Override
  public String toString() {
    return "SensingVoxel{" +
        "sensors=" + sensors +
        '}';
  }
}
