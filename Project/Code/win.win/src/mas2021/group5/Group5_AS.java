package mas2021.group5;

import java.util.List;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;
import genius.core.uncertainty.UserModel;
import genius.core.utility.AbstractUtilitySpace;

public class Group5_AS extends AcceptanceStrategy {
	private Bid receivedBid;
	private Bid lastOwnBid;
	
	private final double alpha = 1.02;
	private final double alpha_init = 0.675;
	private final double beta = 0.005;
	
	// Part of the negotiation time where the agent is in the initialization phase
	private final double time_init = 0.05;
	// Start time where we enter the final phase of the negotiation
	private final double time_final = 0.95;
	
	// In the final phase of the acceptance strategy, calculate the average over this percentage
	private final double windowSlidePercentage = 0.2;
	// This is set once, to determine minimum utility in the final phase.
	private double finalMinimumUtility = -1;
	
	@Override
	public Actions determineAcceptability() {
		receivedBid = negotiationSession.getOpponentBidHistory()
				.getLastBid();
		lastOwnBid = negotiationSession.getOwnBidHistory().getLastBid();
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
			if (Acceptance_Const(alpha_init) && !Acceptance_Time(time_init))
			{
				return Actions.Accept;
			}
			else if ((Acceptance_Next() || (Acceptance_Time(time_final) && Acceptance_Const(getFinalPhaseMinUtility())))
					&& Acceptance_Time(time_init))
			{
				return Actions.Accept;
			}
		}
		return Actions.Reject;
	}
	
	private double getFinalPhaseMinUtility() {
		
		if (this.finalMinimumUtility == -1) {
			final AbstractUtilitySpace utilitySpace = negotiationSession.getUtilitySpace();
			final List<BidDetails> receivedBids = this.negotiationSession.getOpponentBidHistory().getHistory();
			
			final int maxIndex = receivedBids.size() - 1;
			if (maxIndex == -1) {
				// Edge case if receivedBids length is very small.
				this.finalMinimumUtility = 0;
			} else {
				final double minIndexDouble = (1D - this.windowSlidePercentage) * receivedBids.size();
				final int minIndex = (int) Math.floor(minIndexDouble);
				
				final int length = maxIndex - minIndex + 1;
				
				double sum = 0;
				
				for (int index = minIndex; index <= maxIndex; index++) {
					final Bid bid = receivedBids.get(index).getBid();
					sum += utilitySpace.getUtility(bid);
				}
				
				this.finalMinimumUtility = sum / length;
				
			}
		}
		return this.finalMinimumUtility;
	}
	
	/*
	 * Acceptance_Next() compares the bid that was just received with the utility that
	 * the agent is about to send out. If it is the case that alpha times the received
	 * utility is higher than the utility of the bid the agent is planning to send out,
	 * by a factor of at least beta, it will return true to the general acceptance
	 * condition, otherwise it returns false. 
	 */
	public boolean Acceptance_Next()
	{
		double alpha = this.alpha;
		double beta = this.beta;
		double receivedUtil = negotiationSession.getUtilitySpace().getUtility(receivedBid);
		double UtilToSend = negotiationSession.getUtilitySpace().getUtility(lastOwnBid);
		if (alpha * receivedUtil + beta >= UtilToSend)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/*
	 * Returns true in case the received bid has an utility higher than or equal to the constant alpha
	 */
	public boolean Acceptance_Const(double alpha)
	{
		double utility = negotiationSession.getUtilitySpace().getUtility(receivedBid);
		return utility >= alpha;
	}
	
	
	/*
	 * Return true if the current time is past some point T.
	 */
	public boolean Acceptance_Time(double T)
	{
		double time = negotiationSession.getTime();
		return time >= T;
	}

	@Override
	public String getName() {
		return "Group5 win.win Acceptance Strategy";
	}
}