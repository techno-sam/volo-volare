package io.github.slimeistdev.volare.infrastructure;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public interface QuatEntity {
	Quaternionfc getQuat();
	void setQuat(Quaternionf quat);

	Quaternionfc getQuatClient();
	Quaternionfc getQuatClient(float tickProgress);
	void setQuatClient(Quaternionf quat);

	void updateTrackedPositionAndAngles$Quat(Quaternionf quat);
	void refreshPositionAndAngles$Quat(Quaternionf quat);
}
