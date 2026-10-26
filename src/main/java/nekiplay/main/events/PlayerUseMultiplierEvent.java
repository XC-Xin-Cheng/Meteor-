package nekiplay.main.events;

public class PlayerUseMultiplierEvent {
   private float _forward = 0.2F;
   private float _sideways = 0.2F;

   public PlayerUseMultiplierEvent(float forward, float sideways) {
      this._forward = forward;
      this._sideways = sideways;
   }

   public void setForward(float forward) {
      this._forward = forward;
   }

   public float getForward() {
      return this._forward;
   }

   public void setSideways(float sideways) {
      this._sideways = sideways;
   }

   public float getSideways() {
      return this._sideways;
   }
}
