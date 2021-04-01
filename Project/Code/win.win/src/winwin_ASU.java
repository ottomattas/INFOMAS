import java.util.List;

import genius.core.Bid;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;
import genius.core.uncertainty.UserModel;

/**
 * Accepts:
 * <ul>
 * <li>if we have uncertainty profile, and we receive an offer in our highest
 * 10% best bids.
 * <li>if we have normal utilityspace, and we receive offer with a utility
 * better than 90% of what we offered last.
 * </ul>
 * Discount is ignored.
 */
public class winwin_ASU extends AcceptanceStrategy {

	/*
	 * TODO: this function should return Actions.Accept if and only if
	 * AC_combined-init is true (refer to report 2 for a specific description)
	 * for each individual Acceptance condition, there are seperate functions
	 * defined below. 
	 */
	@Override
	public Actions determineAcceptability() {
		Bid receivedBid = negotiationSession.getOpponentBidHistory()
				.getLastBid();
		Bid lastOwnBid = negotiationSession.getOwnBidHistory().getLastBid();
		if (receivedBid == null || lastOwnBid == null) {
			return Actions.Reject;
		}

		UserModel userModel = negotiationSession.getUserModel();
		if (userModel != null) {
			List<Bid> bidOrder = userModel.getBidRanking().getBidOrder();
			if (bidOrder.contains(receivedBid)) {
				double percentile = (bidOrder.size()
						- bidOrder.indexOf(receivedBid))
						/ (double) bidOrder.size();
				if (percentile < 0.1)
					return Actions.Accept;
			}
		} else {
			// we have a normal utilityspace
			double otherLastUtil = negotiationSession.getUtilitySpace()
					.getUtility(receivedBid);
			double myLastUtil = negotiationSession.getUtilitySpace()
					.getUtility(lastOwnBid);
			if (otherLastUtil >= 0.9 * myLastUtil) {
				return Actions.Accept;
			}
		}
		return Actions.Reject;
	}
	
	/*
	 * TODO: This function should look towards the current bid that has been created
	 * by our agent and the bid that we have just received from the opponent. If the 
	 * utility of the opponents bid is higher than our bid, return true, otherwise, 
	 * return false.
	 */
	public boolean Acceptance_Next()
	{
		return false;
	}
	
	/*
	 * TODO: this function should return true if the utility of the received bid 
	 * is higher than some tbd value.The utility that determines this can (and 
	 * quite possibly, should) be dynamic and decreases over time. In the earlier
	 * stages of a negotiation, this should be some constant high utility, in such
	 * a way that it accepts any bid with a utility higher than 0.8, but should become
	 * lower as time passes. 
	 */
	public boolean Acceptance_Const()
	{
		return false;
	}
	
	
	/*
	 * TODO: return true if the current time is past some point T. We should be able to 
	 * change this T after experimentation and to find the best T possible. 
	 */
	public boolean Acceptance_Time()
	{
		return false;
	}

	@Override
	public String getName() {
		return "winwin_AS Uncertainty";
	}
}